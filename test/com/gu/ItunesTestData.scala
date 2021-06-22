package com.gu.itunes

import java.nio.charset.StandardCharsets
import com.google.common.io.Resources
import com.gu.contentapi.client.model.v1.ItemResponse
import com.gu.contentapi.json.CirceDecoders.itemResponseDecoder
import io.circe.{ Decoder, Json }
import io.circe.parser._
import cats.syntax.either._

trait ItunesTestData {

  import JsonHelpers._

  /*
    Search response from CAPI. The URL used is:
    guardianapis.com/science/series/science?show-fields=all&show-elements=audio&show-tags=keyword&page-size=3
  */

  val itunesCapiResponse: ItemResponse = {
    val json = loadJson("itunes-capi-response.json")
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