package com.gu.itunes

import org.jsoup.Jsoup
import org.jsoup.safety.Safelist

object Filtering {

  def standfirst(input: String, preserveHtml: Boolean): String = filter(input, preserveHtml) // standFirst can support html tags

  def description(input: String): String = filter(input, preserveHtml = false) // description should not contain html tags

  private[this] def filter(input: String, preserveHtml: Boolean): String = {

    val doc = Jsoup.parse(input)
    doc.select("br").remove

    val safeList = if (preserveHtml) {
      Safelist.simpleText()
        .addAttributes("a", "href")
        .addEnforcedAttribute("a", "rel", "nofollow")
    } else {
      Safelist.simpleText()
    }

    val cleaned = Jsoup.clean(doc.outerHtml(), safeList)

    if (preserveHtml) {
      Jsoup.parse(cleaned).body.html()
    } else {
      Jsoup.parse(cleaned).text()
    }

  }

}