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

  sealed trait Redirect
  case class TagRedirect(tagId: String) extends Redirect
  case class ExternalRedirect(url: String) extends Redirect

  val redirectsMapping = Map[String, Redirect](
    "film/series/filmweekly" -> TagRedirect("film/series/the-dailies-podcast"),
    "technology/series/techweekly" -> TagRedirect("technology/series/chips-with-everything"),
    "politics/series/politics-for-humans" -> TagRedirect("us-news/series/politics-for-humans"),
    "australia-news/series/token-podcast" -> TagRedirect("society/series/token"),
    "membership/series/guardian-live-podcast" -> TagRedirect("membership/series/we-need-to-talk-about"),
    "music/series/reverberate" -> ExternalRedirect("https://feeds.acast.com/public/shows/e56efa7c-717d-54a0-9d42-2535caea7ccf")
  )

  def redirect(tagId: String): Option[Redirect] = redirectsMapping.get(tagId)

}
