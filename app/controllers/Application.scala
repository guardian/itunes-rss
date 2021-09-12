package com.gu.itunes

import com.gu.contentapi.client.model.{ContentApiError, ItemQuery}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{ DateTime, DateTimeZone }
import org.scalactic.{ Bad, Good }
import play.api.mvc.Results._
import play.api.mvc.{BaseController, ControllerComponents, Result}
import play.api.{Configuration, Logger}

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
    val userAgent = request.headers.get("user-agent").getOrElse("")
    Logger.info(s"Received request for tag '$tagId' from user agent '$userAgent'")

    val redirect = Redirection.redirect(tagId)
    redirect match {
      case Some(redirectedTagId) => Future.successful(MovedPermanently(routes.Application.itunesRss(redirectedTagId, userApiKey).absoluteURL(true)))
      case None =>
        rawRss(tagId, userApiKey)
    }
  }

  private def rawRss(tagId: String, userApiKey: Option[String]): Future[Result] = {
    val client = new CustomCapiClient(apiKey)

    val query = ItemQuery(tagId)
      .showElements("audio")
      .showTags("keyword")
      .showFields("webTitle,webPublicationDate,standfirst,trailText,internalComposerCode")
      .pageSize(200) // number of podcasts to be served (max single request page size)

    (for {
      itemResponse <- client.getResponse(query)
      userApiKeyTier <- userApiKey.map { userApiKey =>
        // If an external partner has identified themselves using an api-key we will query to resolve the user tier
        new CustomCapiClient(userApiKey).getResponse(ItemQuery(tagId).pageSize(0)).map { resp =>
          Some(resp.userTier)
        }
      }.getOrElse {
        Future.successful(None)
      }

    } yield {
      itemResponse.status match {
        case "ok" => {
          val isAdFree = userApiKeyTier.contains("rights-managed")
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
    }).recover {
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
