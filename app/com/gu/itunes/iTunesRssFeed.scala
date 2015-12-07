package com.gu.itunes

import java.text.SimpleDateFormat
import java.util.Date

import com.gu.contentapi.client.model.ItemResponse
import com.gu.contentapi.client.model.v1._
import org.scalactic.{ Bad, Good, Or }

import scala.xml.Node

object iTunesRssFeed {

  def apply(resp: ItemResponse): Node Or String = resp.tag match {
    case Some(t) => toXml(t, resp.results)
    case None => Bad("No tag found")
  }

  def toXml(tag: Tag, podcasts: List[Content]): Node Or String = {

    tag.podcast match {
      case Some(podcast) => Good {
        <rss xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd" version="2.0">
          <channel>
            <title>{ tag.webTitle }</title>
            <link>{ tag.webUrl }</link>
            <description>{ tag.description.getOrElse("") }</description>
            <language>en-gb</language>
            <copyright>{ podcast.copyright }</copyright>
            <lastBuildDate>
              {
                val format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z")
                format.format(new Date())
              }
            </lastBuildDate>
            <ttl>15</ttl>
            <itunes:owner>
              <itunes:email>userhelp@theguardian.com</itunes:email>
              <itunes:name>theguardian.com</itunes:name>
            </itunes:owner>
            <itunes:image href={ podcast.image.getOrElse("") }/>
            <itunes:author>theguardian.com</itunes:author>
            <itunes:explicit>
              { if (podcast.explicit) "yes" else "" }
            </itunes:explicit>
            <itunes:keywords/>
            <itunes:summary>{ tag.description.getOrElse("") }</itunes:summary>
            <image>
              <title>{ tag.webTitle }</title>
              <url>http://static.guim.co.uk/sitecrumbs/Guardian.gif</url>
              <link>http://www.theguardian.com</link>
            </image>
            {
              for (p <- podcasts) yield new iTunesRssItem(p, tag.id).toXml
            }
          </channel>
        </rss>
      }
      case None => Bad {
        "No podcast found"
      }
    }
  }
}

