package com.gu.itunes

import org.scalactic.Bad
import org.scalatest._
import play.api.mvc.Results._

import scala.util.Try
import scala.xml.Utility.trim

class ItunesRssFeedSpec extends FlatSpec with ItunesTestData with Matchers {

  it should "check that the produced XML for the tags is consistent" in {

    val currentXml = trim(iTunesRssFeed(Seq(itunesCapiResponse)).get)

    val expectedXml = trim(
      <rss version="2.0" xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd">
        <channel>
          <title>Science Weekly</title>
          <link>https://www.theguardian.com/science/series/science</link>
          <description>
            The Guardian's science team bring you the best analysis and interviews from the worlds of science and technology
          </description>
          <language>en-gb</language>
          <copyright>theguardian.com © 2014</copyright>
          <lastBuildDate></lastBuildDate>
          <ttl>15</ttl>
          <itunes:type>Serial</itunes:type>
          <itunes:owner>
            <itunes:email>userhelp@theguardian.com</itunes:email>
            <itunes:name>The Guardian</itunes:name>
          </itunes:owner>
          <itunes:image href="https://static.guim.co.uk/sys-images/Guardian/Pix/pictures/2014/4/22/1398182483649/ScienceWeekly.png"/>
          <itunes:author>The Guardian</itunes:author>
          <itunes:keywords/>
          <itunes:summary>
            The Guardian's science team bring you the best analysis and interviews from the worlds of science and technology
          </itunes:summary>
          <itunes:new-feed-url>https://www.theguardian.com/science/series/science/podcast.xml</itunes:new-feed-url>
          <image>
            <title>Science Weekly</title>
            <url>https://static.guim.co.uk/sys-images/Guardian/Pix/pictures/2014/4/22/1398182483649/ScienceWeekly.png</url>
            <link>https://www.theguardian.com/science/series/science</link>
          </image>
          <itunes:category text="Health">
            <itunes:category text="Fitness &amp; Nutrition"/>
          </itunes:category>
        </channel>
      </rss>)

    currentXml \ "channel" \ "title" should be(expectedXml \ "channel" \ "title")
    currentXml \ "channel" \ "link" should be(expectedXml \ "channel" \ "link")
    currentXml \ "channel" \ "description" should be(expectedXml \ "channel" \ "description")
    currentXml \ "channel" \ "language" should be(expectedXml \ "channel" \ "language")
    currentXml \ "channel" \ "copyright" should be(expectedXml \ "channel" \ "copyright")
    currentXml \ "channel" \ "type" should be(expectedXml \ "channel" \ "type")
    currentXml \ "channel" \ "ttl" should be(expectedXml \ "channel" \ "ttl")
    currentXml \ "channel" \ "owner" should be(expectedXml \ "channel" \ "owner")
    currentXml \ "channel" \ "image" should be(expectedXml \ "channel" \ "image")
    currentXml \ "channel" \ "author" should be(expectedXml \ "channel" \ "author")
    currentXml \ "channel" \ "explicit" should be(expectedXml \ "channel" \ "explicit")
    currentXml \ "channel" \ "summary" should be(expectedXml \ "channel" \ "summary")
    currentXml \ "channel" \ "image" should be(expectedXml \ "channel" \ "image")
    currentXml \ "channel" \ "category" should be(expectedXml \ "channel" \ "category")
    currentXml \ "channel" \ "new-feed-url" should be(expectedXml \ "channel" \ "new-feed-url")

    // Channel image link should match channel link
    currentXml \ "channel" \ "image" \ "link" should be(currentXml \ "channel" \ "link")
  }

  it should "return a 404 if a podcast cannot be found" in {
    val attempt = Try(iTunesRssFeed(Seq(tagMissingPodcastFieldResponse)))
    attempt.get match {
      case Bad(failed: Failed) =>
        failed.message should be("podcast not found")
        failed.status should be(NotFound)
        failed.toString should be("message: podcast not found, status: 404")
      case _ =>
        fail("""expected Bad(Failed("podcast not found", NotFound))""")
    }
  }

  it should "mark ad free podcast channels as blocked so that the are not indexed in things like Google podcasts" in {
    // https://developers.google.com/news/assistant/your-news-update/overview
    // To prevent the feed from public availability on products like iTunes or Google Podcasts, the value can be set to Yes (not case sensitive). Any other value has no effect.
    val currentXml = trim(iTunesRssFeed(Seq(itunesCapiResponse), adFree = true).get)

    val channelLevelItunesBlock = (currentXml \\ "channel" \ "block").filter(_.prefix == "itunes").head
    channelLevelItunesBlock.text should be("yes")
  }

  it should "not prevent non ad free podcasts from been indexed" in {
    val currentXml = trim(iTunesRssFeed(Seq(itunesCapiResponse), adFree = false).get)

    val channelLevelItunesBlock = (currentXml \\ "channel" \ "block").filter(_.prefix == "itunes")
    channelLevelItunesBlock.isEmpty should be(true)
  }

  it should "not show new-feed-url tag in ad free feeds to avoid confusing robots" in {
    val currentXml = trim(iTunesRssFeed(Seq(itunesCapiResponse), adFree = true).get)

    val itunesNewFeedUrl = (currentXml \\ "channel" \ "new-feed-url").find(_.prefix == "itunes")
    itunesNewFeedUrl should be(None)
  }

  it should "show large image specific to this podcast on the channel image tag for ad free feeds" in {
    val currentXml = trim(iTunesRssFeed(Seq(itunesCapiResponse), adFree = true).get)

    val channelImageUrl = currentXml \\ "channel" \ "image" \ "url"

    channelImageUrl.text should be("https://static.guim.co.uk/sys-images/Guardian/Pix/pictures/2014/4/22/1398182483649/ScienceWeekly.png")
  }

}
