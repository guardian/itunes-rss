package controllers

import com.gu.contentapi.client.model.ItemQuery
import com.gu.itunes.{ iTunesRssFeed, CustomCapiClient }
import org.scalactic.{ Bad, Good }
import play.api.Play
import play.api.mvc.{ Action, Controller }
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  val apiKey = Play.current.configuration.getString("apiKey").getOrElse("boo")

  def itunesRss(tagId: String) = Action.async {
    val client = new CustomCapiClient(apiKey)

    val query = ItemQuery(tagId)
      .showElements("audio")
      .showTags("all")
      .showFields("all")
      .pageSize(100) // TODO check this value

    client.getResponse(query) map { itemResponse =>
      itemResponse.status match {
        case "ok" => iTunesRssFeed(itemResponse) match {
          case Good(xml) => Ok(xml)
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
