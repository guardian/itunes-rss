package com.gu.itunes

object Redirection {

  /*
    iTunes site manager does not allow to change the XML feed location for an existing podcast.
    this prevents guardian editors to migrate an existing podcast audience to a new tag.    
  */
  def redirect(tagId: String): Option[String] = {
    if (tagId == "film/series/filmweekly") {
      Some("film/series/the-dailies-podcast")
    } else {
      None
    }
  }

}