package com.gu.itunes

import cats.syntax.either._
import com.google.common.io.Resources
import com.gu.contentapi.client.model.v1.ItemResponse
import com.gu.contentapi.json.CirceDecoders.itemResponseDecoder
import io.circe.parser._
import io.circe.{ Decoder, Json }

import java.nio.charset.StandardCharsets

trait ItunesTestData {

  import JsonHelpers._

  /*
    Search response from CAPI. The URL used is:
    guardianapis.com/science/series/science?show-fields=webTitle%2CwebPublicationDate%2Cstandfirst%2CtrailText%2CinternalComposerCode&show-elements=audio&show-tags=keyword&page-size=3
  */

  val itunesCapiResponse: ItemResponse = {
    val json = loadJson("itunes-capi-sparse-response.json")
    parseJson[ItemResponse](json)
  }

  val tagMissingPodcastFieldResponse: ItemResponse = {
    val json = loadJson("itunes-capi-response-no-podcast-tag.json")
    parseJson[ItemResponse](json)
  }

  // content.guardianapis.com/politics/series/brexit-means?show-fields=all&show-elements=audio&show-tags=keyword&page-size=3
  val itunesCapiResponseAcastTest: ItemResponse = {
    val json = loadJson("itunes-capi-response-acast-test.json")
    parseJson[ItemResponse](json)
  }

  // content.guardianapis.com/technology/series/blackbox?show-fields=webTitle%2CwebPublicationDate%2Cstandfirst%2CtrailText%2CinternalComposerCode&show-elements=audio&show-tags=keyword&page-size=3
  // contains episode number in web title
  val itunesCapiResponseEpisodeNumber: ItemResponse = {
    val json = loadJson("itunes-capi-episode-number.json")
    parseJson[ItemResponse](json)
  }

  // content.guardianapis.com/news/series/cotton-capital-podcast?show-fields=webTitle%2CwebPublicationDate%2Cstandfirst%2CtrailText%2CinternalComposerCode&show-elements=audio&show-tags=keyword&page-size=3
  val itunesCapiResponseNoType: ItemResponse = {
    val json = loadJson("itunes-capi-no-type.json")
    parseJson[ItemResponse](json)
  }

}

object JsonHelpers {
  def loadJson(filename: String): String = {
    Resources.toString(Resources.getResource(filename), StandardCharsets.UTF_8)
  }

  def parseJson[T: Decoder](rawJson: String): T = {
    val json = parse(rawJson).leftMap(e => throw e).getOrElse(Json.Null)
    val response = json.hcursor.downField("response").focus.getOrElse(Json.Null)
    response.as[T].toOption.get
  }
}
