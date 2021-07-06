package com.gu.itunes

import com.gu.contentapi.client.model.{ ContentApiError, ItemQuery }
import org.joda.time.format.DateTimeFormat
import org.joda.time.{ DateTime, DateTimeZone }
import org.scalactic.{ Bad, Good }
import play.api.mvc.Results._
import play.api.mvc.{ BaseController, ControllerComponents, Result }
import play.api.{ Configuration, Logger }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class Failed(message: String, status: Status) {
  override val toString: String =
    s"message: $message, status: ${status.header.status}"
}

class Application(val controllerComponents: ControllerComponents, val config: Configuration) extends BaseController {

  val apiKey = config.getOptional[String]("apiKey")
    .getOrElse(sys.error("You must provide a CAPI key, either in application.conf or as the API_KEY environment variable"))

  val maxAge = 300
  val staleWhileRevalidateSeconds = 600
  val oneDayInSeconds = 86400
  val cacheControl = s"max-age=$maxAge, stale-while-revalidate=$staleWhileRevalidateSeconds, stale-if-error=$oneDayInSeconds"
  private val HTTPDateFormat = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZone(DateTimeZone.UTC)

  def itunesRss(tagId: String, userApiKey: Option[String]) = Action.async { implicit request =>
    val redirect = Redirection.redirect(tagId)
    redirect match {
      case Some(redirectedTagId) => Future.successful(MovedPermanently(routes.Application.itunesRss(redirectedTagId, userApiKey).absoluteURL(true)))
      case None =>
        rawRss(tagId, userApiKey)
    }
  }

  private def rawRss(tagId: String, userApiKey: Option[String]): Future[Result] = {
    val apiKeyToUse = userApiKey.getOrElse(apiKey)

    val client = new CustomCapiClient(apiKeyToUse)

    val query = ItemQuery(tagId)
      .showElements("audio")
      .showTags("keyword")
      .showFields("all")
      .pageSize(200) // number of podcasts to be served (max single request page size)

    client.getResponse(query) map { itemResponse =>
      itemResponse.status match {
        case "ok" => {
          // Ad free if a user supplied key partner key has been used
          val isAdFree = userApiKey.exists { _ =>
            itemResponse.userTier == "partner"
          }

          iTunesRssFeed(itemResponse, isAdFree) match {
            case Good(xml) =>
              val now = DateTime.now()
              val expiresTime = now.plusSeconds(maxAge)

              Ok(xml).withHeaders(
                "Surrogate-Control" -> cacheControl,
                "Cache-Control" -> cacheControl,
                "Expires" -> expiresTime.toString(HTTPDateFormat),
                "Date" -> now.toString(HTTPDateFormat))
            case Bad(failed: Failed) =>
              Logger.warn(s"Failed to render XML. tagId = $tagId, ${failed.toString}")
              failed.status
          }
        }
        case _ => NotFound
      }
    } recover {
      case ContentApiError(404, _, _) => NotFound
      case ContentApiError(403, _, _) => Forbidden
      // maybe this generic InternalServerError could be a better representation of the CAPI failure mode
      case ContentApiError(status, msg, errorResponse) =>
        Logger.warn(s"Unexpected response code from CAPI. tagId = $tagId, HTTP status = $status, error response = $errorResponse")
        InternalServerError
    }
  }

  def healthcheck = Action {
    Ok("OK")
  }
}
