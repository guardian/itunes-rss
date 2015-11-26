package com.gu.itunes

import com.gu.contentapi.client.model.v1._
import java.text.SimpleDateFormat
import scala.xml.Node

class iTunesRssItem(val podcast: Content) {

  def toXml: Node = {
    <item>
      <title> { podcast.webTitle } </title>
      <description> { podcast.fields.flatMap(_.standfirst).getOrElse("") } </description>
      <enclosure url=""/>
      <pubDate>
        {
          val dd = podcast.webPublicationDate.map(_.dateTime).getOrElse(0)
          val format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z")
          format.format(dd)
        }
      </pubDate>
      <guid>
        {
          val guid = for {
            asset <- getFirstAsset(podcast)
            guid <- asset.file
          } yield guid

          guid.getOrElse("")
        }
      </guid>
      <itunes:duration>
        {
          val typeData: Option[AssetFields] = for {
            asset <- getFirstAsset(podcast)
            typeData <- asset.typeData
          } yield typeData

          val seconds = typeData.flatMap(_.durationSeconds)
          val minutes = typeData.flatMap(_.durationMinutes)

          convertDate(seconds, minutes)
        }
      </itunes:duration>
      <itunes:author>theguardian.com</itunes:author>
      <itunes:explicit>
        {
          val typeData = for {
            asset <- getFirstAsset(podcast)
            typeData <- asset.typeData
          } yield typeData

          val exp = typeData.flatMap(_.explicit).getOrElse(false)
          if (exp) "yes" else "no"
        }
      </itunes:explicit>
      <itunes:clean>
        {
          val typeData = for {
            asset <- getFirstAsset(podcast)
            typeData <- asset.typeData
          } yield typeData

          val clean = typeData.flatMap(_.clean).getOrElse(false)
          if (clean) "yes" else "no"
        }
      </itunes:clean>
      <itunes:keywords>{ makeKeywordsList(podcast.tags) }</itunes:keywords>
      <itunes:subtitle>{ podcast.fields.flatMap(_.standfirst).getOrElse("") }</itunes:subtitle>
      <itunes:summary>{ podcast.fields.flatMap(_.standfirst).getOrElse("") }</itunes:summary>
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

  private def getFirstAsset(podcast: Content): Option[Asset] = {
    for {
      elements <- podcast.elements
      element <- elements.headOption
      asset <- element.assets.headOption
    } yield asset
  }

  private def makeKeywordsList(tags: Seq[Tag]): String = {
    val keys = for (t <- tags) yield t.webTitle
    keys.mkString(", ")
  }
}
