package controllers

import com.gu.contentapi.client.GuardianContentApiError
import com.gu.contentapi.client.model.ItemQuery
import com.gu.itunes._
import org.joda.time.format.DateTimeFormat
import org.joda.time.{ DateTimeZone, DateTime }
import org.scalactic.{ Bad, Good }
import play.api.Play
import play.api.Logger
import play.api.mvc.{ Action, Controller, Result }
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  val apiKey = Play.current.configuration.getString("apiKey")
    .getOrElse(sys.error("You must provide a CAPI key, either in application.conf or as the API_KEY environment variable"))

  val maxAge = 300
  val staleWhileRevalidateSeconds = 600
  val oneDayInSeconds = 86400
  val cacheControl = s"max-age=$maxAge, stale-while-revalidate=$staleWhileRevalidateSeconds, stale-if-error=$oneDayInSeconds"
  private val HTTPDateFormat = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZone(DateTimeZone.UTC)

  def itunesRss(tagId: String) = Action.async { implicit request =>
    val redirect = Redirection.redirect(tagId)
    redirect match {
      case Some(redirectedTagId) => Future.successful(MovedPermanently(routes.Application.itunesRss(redirectedTagId).absoluteURL()))
      case None => rawRss(tagId)
    }
  }

  def rawRss(tagId: String): Future[Result] = {

    val client = new CustomCapiClient(apiKey)

    val query = ItemQuery(tagId)
      .showElements("audio")
      .showTags("keyword")
      .showFields("all")
      .pageSize(100) // number of podcasts to be served

    client.getResponse(query) map { itemResponse =>
      itemResponse.status match {
        case "ok" => iTunesRssFeed(itemResponse) match {
          case Good(xml) =>
            val now = DateTime.now()
            val expiresTime = now.plusSeconds(maxAge)

            Ok(xml).withHeaders(
              "Surrogate-Control" -> cacheControl,
              "Cache-Control" -> cacheControl,
              "Expires" -> expiresTime.toString(HTTPDateFormat),
              "Date" -> now.toString(HTTPDateFormat)
            )
          case Bad(errorMsg) =>
            Logger.warn(s"Failed to render XML. tagId = $tagId, errorMsg = $errorMsg")
            InternalServerError
        }
        case _ => NotFound
      }
    } recover {
      case GuardianContentApiError(404, _, _) => NotFound
      case GuardianContentApiError(status, msg, errorResponse) =>
        Logger.warn(s"Unexpected response code from CAPI. tagId = $tagId, HTTP status = $status, error response = $errorResponse")
        InternalServerError
    }
  }

  def healthcheck = Action {
    Ok("OK")
  }
}
