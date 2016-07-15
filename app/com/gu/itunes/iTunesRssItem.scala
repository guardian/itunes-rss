package com.gu.itunes

import org.joda.time._
import com.gu.contentapi.client.model.v1._
import com.gu.contentapi.client.utils.CapiModelEnrichment._

import scala.xml.Node

class iTunesRssItem(val podcast: Content, val tagId: String, asset: Asset) {

  def toXml: Node = {

    val title = podcast.webTitle

    val description = Filtering.standfirst(podcast.fields.flatMap(_.standfirst).getOrElse(""))

    val url = asset.file.getOrElse("")

    val sizeInBytes = asset.typeData.flatMap(_.sizeInBytes).getOrElse(0).toString

    val mType = asset.mimeType.getOrElse("")

    val pubDate = {
      val lastModified = podcast.webPublicationDate.map(_.toJodaDateTime).getOrElse(DateTime.now)
      DateSupport.toRssTimeFormat(lastModified)
    }

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

    val summary = Filtering.standfirst(podcast.fields.flatMap(_.standfirst).getOrElse(""))

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
