package com.gu.itunes

import com.gu.contentapi.client.model.v1._
import org.joda.time.DateTime
import org.scalactic.{ Bad, Good, Or }
import play.api.mvc.Results._

import java.time.Instant
import scala.xml.Node

object iTunesRssFeed {

  val author = "The Guardian"

  /* Before 24th December 2021 the podcasts feed had 200 items, but with items
 * 100-200 repeated so it looked like 300 items. Apple seems to have mostly
 * ignored items >100 in the feed, presumably because of how they handle
 * duplicates.
 *
 * On 24th December 2021 we fixed the bug so that the feed had 300 unique items.
 *
 * Apple downloads of any items that were brought back into the feed by this
 * change increased massively.  They have since reduced but not to the level we
 * would expect.  The appleExcessDownloadsWorkaround is intended to allow us to
 * gradually increase the number of podcasts in the feed to 300, without having
 * to bring old items back into the feed.
 *
 * We should be able to remove this workaround at some point in the future, when
 * it is no longer filtering out any items.
 */

  private val feedChangeDate = Instant.parse("2021-12-24T10:30:00Z")

  private def afterFeedChangeDate(capiDateTime: CapiDateTime) =
    Instant.ofEpochMilli(capiDateTime.dateTime).isAfter(feedChangeDate)

  private def appleExcessDownloadsWorkaround(items: List[Content]) = {
    val (afterChange, beforeChange) = items.partition(_.webPublicationDate.exists(afterFeedChangeDate))
    afterChange ++ beforeChange.take(100)
  }

  def apply(resps: Seq[ItemResponse], adFree: Boolean = false, imageResizerSalt: Option[String]): Node Or Failed = {
    val tag = resps.headOption.flatMap(_.tag)
    tag match {
      case Some(t) =>
        val content = resps.flatMap(_.results.getOrElse(Nil)).toList
        toXml(t, appleExcessDownloadsWorkaround(content), adFree, imageResizerSalt)
      case None => Bad(Failed("tag not found", NotFound))
    }
  }

  def toXml(tag: Tag, contents: List[Content], adFree: Boolean, imageResizerSalt: Option[String]): Node Or Failed = {

    val description = Filtering.description(tag.description.getOrElse(""))

    tag.podcast match {
      case Some(podcast) => Good {
        <rss xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd" version="2.0">
          <channel>
            {
              if (!adFree) {
                <itunes:new-feed-url>{ s"${tag.webUrl}/podcast.xml" }</itunes:new-feed-url>
              }
            }
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
              <url>{ podcast.image.getOrElse("https://static.guim.co.uk/sitecrumbs/Guardian.gif") }</url>
              <link>{ tag.webUrl }</link>
            </image>
            {
              if (adFree) {
                <itunes:block>yes</itunes:block>
              }
            }
            {
              for (category <- podcast.categories.getOrElse(Nil)) yield new CategoryRss(category).toXml
            }
            {
              for {
                podcastContent <- contents
                asset <- getFirstAudioAsset(podcastContent)
                element <- getFirstAudioElement(podcastContent)
              } yield new iTunesRssItem(podcastContent, tag.id, asset, element, adFree, Some(podcast), imageResizerSalt).toXml
            }
          </channel>
        </rss>
      }
      case None => Bad {
        Failed("podcast not found", NotFound)
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

  private def getFirstAudioElement(podcast: Content): Option[BlockElement] = {
    for {
      blocks <- podcast.blocks
      main <- blocks.main
      element <- main.elements.find(_.`type` == ElementType.Audio)
    } yield element
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
