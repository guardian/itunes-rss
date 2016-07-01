package com.gu.itunes

import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

object Filtering {

  val whitelist = Whitelist.simpleText().addTags("a")

  def standfirst(input: String): String = filter(input)

  def description(input: String): String = filter(input)

  private[this] def filter(input: String): String = {

    val doc = Jsoup.parse(input)
    doc.select("br").remove

    val cleaned = Jsoup.clean(doc.outerHtml(), whitelist)
    Jsoup.parse(cleaned).text()
  }

}