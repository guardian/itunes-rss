package com.gu.itunes

import com.gu.contentapi.client.model.v1.ItemResponse
import com.gu.contentapi.client.model.v1._
import org.joda.time.DateTime
import org.scalactic.{ Bad, Good, Or }

import scala.xml.Node

object iTunesRssFeed {

  def apply(resp: ItemResponse): Node Or String = resp.tag match {
    case Some(t) => toXml(t, resp.results.getOrElse(Nil).toList)
    case None => Bad("No tag found")
  }

  def toXml(tag: Tag, contents: List[Content]): Node Or String = {

    val description = Filtering.description(tag.description.getOrElse(""))

    val author = if (tag.id == "society/series/token" || tag.id == "books/series/books") "The Guardian" else "theguardian.com"

    tag.podcast match {
      case Some(podcast) => Good {
        <rss xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd" version="2.0">
          <channel>
            <itunes:new-feed-url>{ s"${tag.webUrl}/podcast.xml" }</itunes:new-feed-url>
            <title>{ tag.webTitle }</title>
            <link>{ tag.webUrl }</link>
            <description>{ description }</description>
            <language>en-gb</language>
            <copyright>{ podcast.copyright }</copyright>
            <lastBuildDate>
              { DateSupport.toRssTimeFormat(DateTime.now) }
            </lastBuildDate>
            <ttl>15</ttl>
            {
              podcast.podcastType match {
                case Some(value) => <itunes:type>{ value }</itunes:type>
                case None =>
              }
            }
            <itunes:owner>
              <itunes:email>userhelp@theguardian.com</itunes:email>
              <itunes:name>{ author }</itunes:name>
            </itunes:owner>
            <itunes:image href={ podcast.image.getOrElse("") }/>
            <itunes:author>{ author }</itunes:author>
            {
              if (podcast.explicit)
                <itunes:explicit>yes</itunes:explicit>
            }
            <itunes:keywords/>
            <itunes:summary>{ description }</itunes:summary>
            <image>
              <title>{ tag.webTitle }</title>
              <url>https://static.guim.co.uk/sitecrumbs/Guardian.gif</url>
              <link>https://www.theguardian.com</link>
            </image>
            {
              for (category <- podcast.categories.getOrElse(Nil)) yield new CategoryRss(category).toXml
            }
            {
              for {
                podcast <- contents
                asset <- getFirstAudioAsset(podcast)
              } yield new iTunesRssItem(podcast, tag.id, author, asset).toXml
            }
          </channel>
        </rss>
      }
      case None => Bad {
        "No podcast found"
      }
    }
  }

  private def getFirstAudioAsset(podcast: Content): Option[Asset] = {
    // should contain at least one audio asset
    for {
      elements <- podcast.elements
      element <- elements.headOption
      asset <- element.assets.find(_.`type` == AssetType.Audio)
    } yield asset
  }

}

class CategoryRss(val category: PodcastCategory) {
  def toXml: Node = {
    <itunes:category text={ category.main }>
      {
        category.sub match {
          case Some(s) => <itunes:category text={ s }/>
          case None =>
        }
      }
    </itunes:category>
  }

}
