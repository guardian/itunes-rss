package com.gu.itunes

import org.scalatest._
import scala.xml.Utility.trim

class ItunesRssFeedSpec extends FlatSpec with ItunesTestData with Matchers {

  it should "check that the produced XML for the tags is consistent" in {

    val currentXml = trim(iTunesRssFeed(itunesCapiResponse).get)

    val expectedXml = trim(
      <rss version="2.0" xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd">
        <channel>
          <title>Science Weekly</title>
          <link>http://www.theguardian.com/science/series/science</link>
          <description>
            The Guardian's science team bring you the best analysis and interviews from the worlds of science and technology
          </description>
          <language>en-gb</language>
          <copyright>theguardian.com © 2014</copyright>
          <lastBuildDate></lastBuildDate>
          <ttl>15</ttl>
          <itunes:owner>
            <itunes:email>userhelp@theguardian.com</itunes:email>
            <itunes:name>theguardian.com</itunes:name>
          </itunes:owner>
          <itunes:image href="http://static.guim.co.uk/sys-images/Guardian/Pix/pictures/2014/4/22/1398182483649/ScienceWeekly.png"/>
          <itunes:author>theguardian.com</itunes:author>
          <itunes:keywords/>
          <itunes:summary>
            The Guardian's science team bring you the best analysis and interviews from the worlds of science and technology
            <br/>
            <br/>
            <![CDATA[<a href="https://membership.theguardian.com/supporter?INTCMP=MEMBERSHIP_SUPPORTER_PODCASTS">Enjoy our podcasts? support us!</a>]]>
          </itunes:summary>
          <image>
            <title>Science Weekly</title>
            <url>http://static.guim.co.uk/sitecrumbs/Guardian.gif</url>
            <link>http://www.theguardian.com</link>
          </image>
          <itunes:category text="Health">
            <itunes:category text="Fitness &amp; Nutrition"/>
          </itunes:category>
        </channel>
      </rss>
    )

    expectedXml \ "channel" \ "title" should be(currentXml \ "channel" \ "title")
    expectedXml \ "channel" \ "link" should be(currentXml \ "channel" \ "link")
    expectedXml \ "channel" \ "description" should be(currentXml \ "channel" \ "description")
    expectedXml \ "channel" \ "language" should be(currentXml \ "channel" \ "language")
    expectedXml \ "channel" \ "copyright" should be(currentXml \ "channel" \ "copyright")
    expectedXml \ "channel" \ "ttl" should be(currentXml \ "channel" \ "ttl")
    expectedXml \ "channel" \ "owner" should be(currentXml \ "channel" \ "owner")
    expectedXml \ "channel" \ "image" should be(currentXml \ "channel" \ "image")
    expectedXml \ "channel" \ "author" should be(currentXml \ "channel" \ "author")
    expectedXml \ "channel" \ "explicit" should be(currentXml \ "channel" \ "explicit")
    expectedXml \ "channel" \ "summary" should be(currentXml \ "channel" \ "summary")
    expectedXml \ "channel" \ "image" should be(currentXml \ "channel" \ "image")
    expectedXml \ "channel" \ "category" should be(currentXml \ "channel" \ "category")
  }
}
