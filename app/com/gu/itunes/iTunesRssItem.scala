package com.gu.itunes

import org.joda.time._
import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.client.utils.CapiModelEnrichment._

import scala.xml.Node

class iTunesRssItem(val podcast: Content, val tagId: String, asset: Asset) {

  def toXml: Node = {

    val title = podcast.webTitle

    val lastModified = podcast.webPublicationDate.map(_.toJodaDateTime).getOrElse(DateTime.now)

    val pubDate = DateSupport.toRssTimeFormat(lastModified)

    val membershipCta = {
      val launchDay = new DateTime(2016, 12, 6, 0, 0)
      if (lastModified.isAfter(launchDay) && tagId == "politics/series/politicsweekly") {
        """. Please support our work and help us keep the world informed. To fund us, go to https://gu.com/give/podcast"""
      } else {
        ""
      }
    }

    def acastProxy(url: String): String = {
      case class AcastLaunchGroup(launchDate: DateTime, tagIds: Seq[String])

      val acastPodcasts: Seq[AcastLaunchGroup] = List(
        AcastLaunchGroup(new DateTime(2017, 4, 19, 0, 0), Seq("technology/series/chips-with-everything")),
        AcastLaunchGroup(new DateTime(2017, 5, 2, 0, 0), Seq(
          "football/series/footballweekly",
          "news/series/the-audio-long-read",
          "science/series/science",
          "politics/series/politicsweekly",
          "arts/series/culture",
          "books/series/books",
          "technology/series/chips-with-everything",
          "society/series/token"
        )
        ),
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
          "us-news/series/politics-for-humans"
        )
        )
      )
      val useAcastProxy: Boolean = acastPodcasts.find(_.tagIds.contains(tagId)).exists(p => lastModified.isAfter(p.launchDate))
      if (useAcastProxy) "https://flex.acast.com/" + url.replace("https://", "") else url

    }

    val description = Filtering.standfirst(podcast.fields.flatMap(_.standfirst).getOrElse("")) + membershipCta

    val url = acastProxy(asset.file.getOrElse(""))

    val sizeInBytes = asset.typeData.flatMap(_.sizeInBytes).getOrElse(0).toString

    val mType = asset.mimeType.getOrElse("")

    /* Old content served from http(s)://static(-secure).guim.co.uk/{...} will have the guid field set
    to http://download.guardian.co.uk/{...} for legacy reasons (to match the R2 implementation);
    new content served from https://audio.guim.co.uk will preserve its structure. */

    val capiUrl = asset.file.getOrElse("")
    val regex = s"""https?://static(-secure)?.guim.co.uk/audio/kip/$tagId"""
    val guid = capiUrl.replaceAll(regex, "http://download.guardian.co.uk/draft/audio")

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

    val subtitle = Filtering.standfirst(podcast.fields.flatMap(_.standfirst).getOrElse(""))

    val summary = Filtering.standfirst(podcast.fields.flatMap(_.standfirst).getOrElse("")) + membershipCta

    <item>
      <title> { title } </title>
      <description> { description } </description>
      <enclosure url={ url } length={ sizeInBytes } type={ mType }/>
      <pubDate>{ pubDate }</pubDate>
      <guid>{ guid }</guid>
      <itunes:duration>{ duration }</itunes:duration>
      <itunes:author>theguardian.com</itunes:author>
      {
        explicit match {
          case Some(value) => <itunes:explicit>{ value }</itunes:explicit>
          case None =>
        }
      }
      <itunes:keywords>{ keywords }</itunes:keywords>
      <itunes:subtitle>{ subtitle }</itunes:subtitle>
      <itunes:summary>{ summary }</itunes:summary>
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
