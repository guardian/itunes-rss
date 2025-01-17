package com.gu.itunes

import org.jsoup.Jsoup
import org.jsoup.safety.Safelist

object Filtering {

  private val defaultSafeList = Safelist.simpleText().addTags("a")

  def standfirst(input: String, asHtml: Boolean): String = filter(input, asHtml) // standFirst asHtml preserves links etc

  def description(input: String, asHtml: Boolean): String = filter(input, asHtml) // description should not contain html

  private[this] def filter(input: String, asHtml: Boolean): String = {

    val doc = Jsoup.parse(input)
    doc.select("br").remove

    val safeList = if (asHtml) defaultSafeList.addAttributes("a", "href") else defaultSafeList

    val cleaned = Jsoup.clean(doc.outerHtml(), safeList)
    if (asHtml) {
      Jsoup.parse(cleaned).body().html()
    } else {
      Jsoup.parse(cleaned).text()
    }

  }

}