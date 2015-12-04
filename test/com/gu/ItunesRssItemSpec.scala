package com.gu.itunes

import com.gu.itunes.XmlTestUtils.RemoveWhitespace
import org.scalatest._
import scala.xml.Utility.trim

class ItunesRssPodcastsSpec extends FlatSpec with ItunesTestData with Matchers {

  it should "check that the produced XML for the podcasts is consistent" in {

    val results = itunesCapiResponse.results
    val podcasts = for (p <- results) yield new iTunesRssItem(p).toXml
    val trimmedPodcasts = for (p <- podcasts) yield trim(p)

    val expectedXml = RemoveWhitespace.transform(
      <item>
        <title>
          Inside the mind of renowned mathematician John Conway - podcast
        </title>
        <description>
          John Conway sheds light on the true nature of numbers, the beauty lying within maths and why game-playing is so important to mathematical discovery
        </description>
        <enclosure url="http://static.guim.co.uk/audio/kip/science/series/science/1447948283860/6835/gdn.sci.151120.ic.Science_Weekly_2.mp3" length="0" type="audio/mpeg"/>
        <pubDate>Fri, 20 Nov 2015 07:30:00 GMT</pubDate>
        <guid>
          http://static.guim.co.uk/audio/kip/science/series/science/1447948283860/6835/gdn.sci.151120.ic.Science_Weekly_2.mp3
        </guid>
        <itunes:duration>00:29:07</itunes:duration>
        <itunes:author>theguardian.com</itunes:author>
        <itunes:explicit></itunes:explicit>
        <itunes:keywords>Science, Mathematics</itunes:keywords>
        <itunes:subtitle>
          John Conway sheds light on the true nature of numbers, the beauty lying within maths and why game-playing is so important to mathematical discovery
        </itunes:subtitle>
        <itunes:summary>
          John Conway sheds light on the true nature of numbers, the beauty lying within maths and why game-playing is so important to mathematical discovery
        </itunes:summary>
      </item>
      <item>
        <title>Why are conspiracy theories so attractive? podcast</title>
        <description>
          Should we distrust our own ability to reason? Why is debunking conspiracy theories such a risky business? And is David Icke a force for good?
        </description>
        <enclosure url="http://static.guim.co.uk/audio/kip/science/series/science/1447432633353/5114/gdn.sci.151116.ic.Science_Weekly.mp3" length="0" type="audio/mpeg"/>
        <pubDate>Fri, 13 Nov 2015 17:11:00 GMT</pubDate>
        <guid>
          http://static.guim.co.uk/audio/kip/science/series/science/1447432633353/5114/gdn.sci.151116.ic.Science_Weekly.mp3
        </guid>
        <itunes:duration>00:27:00</itunes:duration>
        <itunes:author>theguardian.com</itunes:author>
        <itunes:explicit></itunes:explicit>
        <itunes:keywords>Science, Psychology</itunes:keywords>
        <itunes:subtitle>
          Should we distrust our own ability to reason? Why is debunking conspiracy theories such a risky business? And is David Icke a force for good?
        </itunes:subtitle>
        <itunes:summary>
          Should we distrust our own ability to reason? Why is debunking conspiracy theories such a risky business? And is David Icke a force for good?
        </itunes:summary>
      </item>
      <item>
        <title>The story of our brains - podcast</title>
        <description>
          Neuroscientist David Eagleman discusses how neuroscience and technology are reshaping how we understand our brains
        </description>
        <enclosure url="http://static.guim.co.uk/audio/kip/science/series/science/1446638390950/3741/gdn.sci.151106.ic.Science_Weekly.mp3" length="0" type="audio/mpeg"/>
        <pubDate>Fri, 06 Nov 2015 07:30:00 GMT</pubDate>
        <guid>
          http://static.guim.co.uk/audio/kip/science/series/science/1446638390950/3741/gdn.sci.151106.ic.Science_Weekly.mp3
        </guid>
        <itunes:duration>00:25:37</itunes:duration>
        <itunes:author>theguardian.com</itunes:author>
        <itunes:explicit></itunes:explicit>
        <itunes:keywords>Science, David Eagleman, Neuroscience</itunes:keywords>
        <itunes:subtitle>
          Neuroscientist David Eagleman discusses how neuroscience and technology are reshaping how we understand our brains
        </itunes:subtitle>
        <itunes:summary>
          Neuroscientist David Eagleman discusses how neuroscience and technology are reshaping how we understand our brains
        </itunes:summary>
      </item>
    )
    val result = trimmedPodcasts zip expectedXml

    result foreach (x => x._1 \ "title" should be(x._2 \ "title"))
    result foreach (x => x._1 \ "description" should be(x._2 \ "description"))
    result foreach (x => x._1 \ "pubDate" should be(x._2 \ "pubDate"))
    result foreach (x => x._1 \ "guid" should be(x._2 \ "guid"))
    result foreach (x => x._1 \ "duration" should be(x._2 \ "duration"))
    result foreach (x => x._1 \ "author" should be(x._2 \ "author"))
    result foreach (x => x._1 \ "explicit" should be(x._2 \ "explicit"))
    result foreach (x => x._1 \ "keywords" should be(x._2 \ "keywords"))
    result foreach (x => x._1 \ "subtitle" should be(x._2 \ "subtitle"))
    result foreach (x => x._1 \ "summary" should be(x._2 \ "summary"))
  }
}
