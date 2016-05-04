package com.gu.itunes

import com.google.common.io.Resources
import com.google.common.base.Charsets
import com.gu.contentapi.client.model.v1.ItemResponse
import com.gu.contentapi.json.JsonParser

trait ItunesTestData {

  /*
    Search response from CAPI. The URL used is:
    guardianapis.com/science/series/science?show-fields=all&show-elements=audio&show-tags=keyword&page-size=3
  */

  val itunesCapiResponse: ItemResponse = {
    val json = Resources.toString(Resources.getResource("itunes-capi-response.json"), Charsets.UTF_8)
    JsonParser.parseItem(json)
  }
}
