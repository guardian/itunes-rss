package com.gu.itunes

import org.joda.time._
import com.gu.contentapi.client.model.v1._

import scala.xml.Node

class iTunesRssItem(val podcast: Content, val tagId: String, asset: Asset) {

  private val standfirstOrTrail = podcast.fields.flatMap(_.standfirst) orElse podcast.fields.flatMap(_.trailText)

  def toXml: Node = {

    // TODO: remove the below when suffix is added only where it is needed, and not by journalists
    val suffix = """(.*) [-–—|] podcast$""".r
    val title = podcast.webTitle match { case suffix(prefix) => prefix; case otherwise => otherwise }

    val lastModified = podcast.webPublicationDate.map(date => new DateTime(date.dateTime)).getOrElse(DateTime.now)

    val pubDate = DateSupport.toRssTimeFormat(lastModified)

    val membershipCta = {
      val theMomentFrom = new DateTime(2019, 2, 20, 0, 0)
      val launchDayTIF = new DateTime(2018, 11, 14, 0, 0)
      val launchDayPW = new DateTime(2016, 12, 6, 0, 0)
      val launchDayPWNew = new DateTime(2018, 11, 15, 0, 0)
      val footballWeekly = new DateTime(2019, 4, 4, 0, 0)

      if (tagId == "politics/series/politicsweekly") {
        if (lastModified.isAfter(theMomentFrom))
          """. Help support our independent journalism at <a href="https://www.theguardian.com/politicspod">theguardian.com/politicspod</a>"""
        else if (lastModified.isAfter(launchDayPWNew))
          """. To support The Guardian’s independent journalism, visit <a href="https://www.theguardian.com/give/podcast">theguardian.com/give/podcast</a>"""
        else if (lastModified.isAfter(launchDayPW))
          """. Please support our work and help us keep the world informed. To fund us, go to https://www.theguardian.com/give/podcast"""
        else
          ""
      } else if (tagId == "news/series/todayinfocus") {
        if (lastModified.isAfter(theMomentFrom))
          """. Help support our independent journalism at <a href="https://www.theguardian.com/infocus">theguardian.com/infocus</a>"""
        else if (lastModified.isAfter(launchDayTIF))
          """. To support The Guardian’s independent journalism, visit <a href="https://www.theguardian.com/todayinfocus/support">theguardian.com/todayinfocus/support</a>"""
        else
          ""
      } else if (tagId == "books/series/books") {
        if (lastModified.isAfter(theMomentFrom))
          """. Help support our independent journalism at <a href="https://www.theguardian.com/bookspod">theguardian.com/bookspod</a>"""
        else
          ""
      } else if (tagId == "news/series/the-audio-long-read") {
        if (lastModified.isAfter(theMomentFrom))
          """. Help support our independent journalism at <a href="https://www.theguardian.com/longreadpod">theguardian.com/longreadpod</a>"""
        else
          ""
      } else if (tagId == "science/series/science") {
        if (lastModified.isAfter(theMomentFrom))
          """. Help support our independent journalism at <a href="https://www.theguardian.com/sciencepod">theguardian.com/sciencepod</a>"""
        else
          ""
      } else if (tagId == "technology/series/chips-with-everything") {
        if (lastModified.isAfter(theMomentFrom))
          """. Help support our independent journalism at <a href="https://www.theguardian.com/chipspod">theguardian.com/chipspod</a>"""
        else
          ""
      } else if (tagId == "football/series/footballweekly") {
        if (lastModified.isAfter(footballWeekly))
          """. Help support our independent journalism at <a href="https://www.theguardian.com/footballweeklypod">theguardian.com/footballweeklypod</a>"""
        else ""
      } else {
        ""
      }
    }

    def acastProxy(url: String): String = {
      case class AcastLaunchGroup(launchDate: DateTime, tagIds: Seq[String])

      val acastPodcasts: Seq[AcastLaunchGroup] = Seq(
        AcastLaunchGroup(new DateTime(2017, 4, 19, 0, 0), Seq("technology/series/chips-with-everything")),
        AcastLaunchGroup(new DateTime(2017, 5, 2, 0, 0), Seq(
          "football/series/footballweekly",
          "news/series/the-audio-long-read",
          "science/series/science",
          "politics/series/politicsweekly",
          "arts/series/culture",
          "books/series/books",
          "technology/series/chips-with-everything",
          "society/series/token")),
        AcastLaunchGroup(new DateTime(2017, 6, 12, 0, 0), Seq(
          "politics/series/brexit-means",
          "global-development/series/global-development-podcast",
          "news/series/the-story",
          "lifeandstyle/series/close-encounters",
          "music/series/musicweekly",
          "lifeandstyle/series/guardian-guide-to-running-podcast-beginner",
          "commentisfree/series/what-would-a-feminist-do",
          "tv-and-radio/series/game-of-thrones-the-citadel-podcast",
          "australia-news/series/australian-politics-live",
          "australia-news/series/behind-the-lines-podcast",
          "artanddesign/series/guardian-australia-culture-podcast",
          "film/series/the-dailies-podcast",
          "world/series/project",
          "us-news/series/politics-for-humans")),
        AcastLaunchGroup(new DateTime(2018, 2, 21, 0, 0), Seq(
          "culture/series/thestart")),
        AcastLaunchGroup(new DateTime(2018, 5, 30, 0, 0), Seq(
          "australia-news/series/the-reckoning-guardian-australia-podcast")),
        AcastLaunchGroup(new DateTime(2018, 5, 29, 0, 0), Seq(
          "membership/series/we-need-to-talk-about")),
        AcastLaunchGroup(new DateTime(2018, 9, 13, 0, 0), Seq(
          "society/series/beyondtheblade")),
        AcastLaunchGroup(new DateTime(2018, 10, 25, 0, 0), Seq(
          "news/series/todayinfocus")),
        AcastLaunchGroup(new DateTime(2018, 11, 24, 0, 0), Seq(
          "australia-news/series/witch-hunt")),
        AcastLaunchGroup(new DateTime(2019, 1, 28, 0, 0), Seq(
          "environment/series/look-at-me")),
        AcastLaunchGroup(new DateTime(2019, 5, 23, 0, 0), Seq(
          "sport/series/the-spin-podcast")),
        AcastLaunchGroup(new DateTime(2019, 10, 7, 0, 0), Seq(
          "australia-news/series/full-story")),
        AcastLaunchGroup(new DateTime(2020, 1, 28, 0, 0), Seq(
          "science/series/thegenegapcommonsthreads")))
      val useAcastProxy: Boolean = acastPodcasts.find(_.tagIds.contains(tagId)).exists(p => lastModified.isAfter(p.launchDate))
      if (useAcastProxy) "https://flex.acast.com/" + url.replace("https://", "") else url

    }

    val description = Filtering.standfirst(standfirstOrTrail.getOrElse("")) + membershipCta

    val url = acastProxy(asset.file.getOrElse(""))

    val sizeInBytes = asset.typeData.flatMap(_.sizeInBytes).getOrElse(0).toString

    val mType = asset.mimeType.getOrElse("")

    /* Old content served from http(s)://static(-secure).guim.co.uk/{...} will have the guid field set
    to http://download.guardian.co.uk/{...} for legacy reasons (to match the R2 implementation);
    new content served from https://audio.guim.co.uk will preserve its structure. */

    val capiUrl = asset.file.getOrElse("")
    val regex = s"""https?://static(-secure)?.guim.co.uk/audio/kip/$tagId"""
    val guid = {
      val launchDay = new DateTime(2018, 11, 14, 0, 0)
      val default = capiUrl.replaceAll(regex, "http://download.guardian.co.uk/draft/audio")
      if (lastModified.isAfter(launchDay))
        podcast.fields.flatMap(_.internalComposerCode).getOrElse(default)
      else
        default
    }

    val duration = {
      val seconds = asset.typeData.flatMap(_.durationSeconds)
      val minutes = asset.typeData.flatMap(_.durationMinutes)
      convertDate(seconds, minutes)
    }

    val explicit = {
      val exp = asset.typeData.flatMap(_.explicit).getOrElse(false)
      val cln = asset.typeData.flatMap(_.clean).getOrElse(false)
      if (exp) Some("yes") else if (cln) Some("clean") else None
    }

    val keywords = makeKeywordsList(podcast.tags)

    val subtitle = Filtering.standfirst(standfirstOrTrail.getOrElse(""))

    val summary = Filtering.standfirst(standfirstOrTrail.getOrElse("")) + membershipCta

    <item>
      <title> { title } </title>
      <description> { description } </description>
      <enclosure url={ url } length={ sizeInBytes } type={ mType }/>
      <pubDate>{ pubDate }</pubDate>
      <guid>{ guid }</guid>
      <itunes:duration>{ duration }</itunes:duration>
      <itunes:author>{ iTunesRssFeed.author }</itunes:author>
      {
        explicit match {
          case Some(value) => <itunes:explicit>{ value }</itunes:explicit>
          case None =>
        }
      }
      <itunes:keywords>{ keywords }</itunes:keywords>
      <itunes:subtitle>{ subtitle }</itunes:subtitle>
      <itunes:summary>{ scala.xml.Utility.escape(summary) }</itunes:summary>
    </item>
  }

  // convert seconds[Int] and minutes[Int] into HH:MM:SS[String]
  private def convertDate(seconds: Option[Int], minutes: Option[Int]): String = {
    val totalSec = minutes.getOrElse(0) * 60 + seconds.getOrElse(0)
    val hrs = totalSec / 3600
    val rst = totalSec % 3600
    val min = rst / 60
    val sec = rst % 60

    s"${if (hrs < 10) "0" + hrs else hrs}:${if (min <= 9) "0" + min else min}:${if (sec <= 9) "0" + sec else sec}"
  }

  private def makeKeywordsList(tags: Seq[Tag]): String = {
    val keys = for (t <- tags) yield t.webTitle
    keys.mkString(", ")
  }
}
