package com.gu.itunes

import com.gu.contentapi.client.model.v1.ItemResponse
import com.gu.contentapi.client.model.{ ContentApiError, ItemQuery }

import org.joda.time.format.DateTimeFormat
import org.joda.time.{ DateTime, DateTimeZone, Duration }
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
    val startTime = DateTime.now
    val userAgent = request.headers.get("user-agent").getOrElse("")
    Logger.info(s"Received request for tag '$tagId' from user agent '$userAgent'")

    val redirect = Redirection.redirect(tagId)
    val eventualResult = redirect match {
      case Some(redirectedTagId) => Future.successful(MovedPermanently(routes.Application.itunesRss(redirectedTagId, userApiKey).absoluteURL(true)))
      case None =>
        rawRss(tagId, userApiKey)
    }

    eventualResult.map { result =>
      Logger.info(s"Returning response status ${result.header.status} for tag '${tagId} after ${durationSince(startTime)}")
      result
    }.recover {
      case t: Throwable =>
        Logger.warn(s"Failed to complete for tag '$tagId after ${durationSince(startTime)}", t)
        InternalServerError("Could not complete request")
    }
  }

  private def rawRss(tagId: String, userApiKey: Option[String]): Future[Result] = {
    val client = new CustomCapiClient(apiKey)

    val maxItems = 300
    val pageSize = 100

    val query = ItemQuery(tagId)
      .showElements("audio")
      .showTags("keyword")
      .showFields("webTitle,webPublicationDate,standfirst,trailText,internalComposerCode")

    def fetchItemsWithPagination(query: ItemQuery, page: Int = 1, resps: Seq[ItemResponse] = Seq.empty): Future[Seq[ItemResponse]] = {
      Logger.debug("Fetching page: " + page + " with page size: " + pageSize)
      val withPagination = query.page(page).pageSize(pageSize)

      client.getResponse(withPagination).flatMap { resp =>
        val responses = resps :+ resp
        // Paginate if we have not covered the required number of pages and there are more pages available
        val lastRequiredPage = (maxItems / pageSize) + (if (maxItems % pageSize > 0) { 1 } else { 0 })
        val shouldPaginate = (page < lastRequiredPage) && resp.pages.getOrElse(0) > page
        if (shouldPaginate) {
          // Recurse with the results and pagination incremented
          fetchItemsWithPagination(query, page + 1, responses)

        } else {
          Logger.info("Finished fetching " + responses.map(_.results.map(_.size).getOrElse(0)).sum + " items after paginating to page " + page)
          Future.successful(responses)
        }
      }
    }

    (for {
      itemResponses <- fetchItemsWithPagination(query)
      userApiKeyTier <- userApiKey.map { userApiKey =>
        // If an external partner has identified themselves using an api-key we will query to resolve the user tier
        new CustomCapiClient(userApiKey).getResponse(ItemQuery(tagId).pageSize(0)).map { resp =>
          Some(resp.userTier)
        }
      }.getOrElse {
        Future.successful(None)
      }

    } yield {
      // If all item responses were ok then we can render a result
      if (itemResponses.forall(_.status == "ok")) {
        val isAdFree = userApiKeyTier.contains("rights-managed")
        iTunesRssFeed(itemResponses, isAdFree) match {
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

      } else {
        NotFound
      }

    }).recover {
      case ContentApiError(404, _, _) => NotFound
      case ContentApiError(403, _, _) => Forbidden
      case ContentApiError(401, _, _) => Unauthorized
      // maybe this generic InternalServerError could be a better representation of the CAPI failure mode
      case ContentApiError(status, msg, errorResponse) =>
        Logger.warn(s"Unexpected response code from CAPI. tagId = $tagId, HTTP status = $status, error response = $errorResponse")
        InternalServerError
    }
  }

  def healthcheck = Action {
    Ok("OK")
  }

  private def durationSince(time: DateTime): String = new Duration(time, DateTime.now).getMillis + "ms"

}
