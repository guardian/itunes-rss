package controllers

import com.gu.contentapi.client.model.ItemQuery
import com.gu.itunes.{ iTunesRssFeed, CustomCapiClient }
import org.joda.time.format.DateTimeFormat
import org.joda.time.{ DateTimeZone, DateTime }
import org.scalactic.{ Bad, Good }
import play.api.Play
import play.api.mvc.{ Action, Controller }
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  val apiKey = Play.current.configuration.getString("apiKey").getOrElse("boo")

  val maxAge = 300
  val staleWhileRevalidateSeconds = 600
  val oneDayInSeconds = 86400
  val cacheControl = s"max-age=$maxAge, stale-while-revalidate=$staleWhileRevalidateSeconds, stale-if-error=$oneDayInSeconds"
  private val HTTPDateFormat = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZone(DateTimeZone.UTC)

  def itunesRss(tagId: String) = Action.async {
    val client = new CustomCapiClient(apiKey)

    val query = ItemQuery(tagId)
      .showElements("audio")
      .showTags("keyword")
      .showFields("all")
      .pageSize(2) // TODO check this value

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
          case Bad(errorMsg) => NotFound
        }
        case _ => NotFound
      }
    }
  }

  def healthcheck = Action {
    Ok("OK")
  }
}
