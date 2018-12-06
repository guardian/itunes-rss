package com.gu.itunes

object Redirection {

  /*
    To update a RSS feed URL (it happens when editors want to change tags) we need to either:
      1. return a 301 redirect response for the old feed to the new feed
      2. use the <itunes:new-feed-url> tag in the new feed to point to the new URL

    We're using 301 redirects when we change tags, and the new-feed-url tag to tell Apple
    to update our feeds to use https.

    Documentation: https://help.apple.com/itc/podcasts_connect/#/itca489031e0
  */

  val BaseUrl = "https://www.theguardian.com"

  val redirectsMapping = Map[String, String](
    "film/series/filmweekly" -> "film/series/the-dailies-podcast",
    "technology/series/techweekly" -> "technology/series/chips-with-everything",
    "politics/series/politics-for-humans" -> "us-news/series/politics-for-humans",
    "australia-news/series/token-podcast" -> "society/series/token",
    "membership/series/guardian-live-podcast" -> "membership/series/we-need-to-talk-about")

  def redirect(tagId: String): Option[String] = redirectsMapping.get(tagId)

}
