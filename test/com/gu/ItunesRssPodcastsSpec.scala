package com.gu.itunes

import com.gu.itunes.XmlTestUtils.RemoveWhitespace
import org.scalatest._
import scala.xml.Utility.trim

class ItunesRssPodcastsSpec extends FlatSpec with ItunesTestData with Matchers {

  it should "check that the produced XML for the podcasts is consistent" in {

    val results = itunesCapiResponse.results
    val podcasts = for (p <- results) yield new iTunesRssItem(p).toXml
    val trimmedPodcasts = for (p <- podcasts) yield trim(p)

    trimmedPodcasts should be(
      RemoveWhitespace.transform(
        <item>
          <title>
            Inside the mind of renowned mathematician John Conway - podcast
          </title>
          <description>
            John Conway sheds light on the true nature of numbers, the beauty lying within maths and why game-playing is so important to mathematical discovery
          </description>
          <enclosure url=""/>
          <pubDate>Fri, 20 Nov 2015 07:30:00 GMT</pubDate>
          <guid>
            http://static.guim.co.uk/audio/kip/science/series/science/1447948283860/6835/gdn.sci.151120.ic.Science_Weekly_2.mp3
          </guid>
          <itunes:duration>00:29:07</itunes:duration>
          <itunes:author>theguardian.com</itunes:author>
          <itunes:explicit>no</itunes:explicit>
          <itunes:clean>no</itunes:clean>
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
          <enclosure url=""/>
          <pubDate>Fri, 13 Nov 2015 17:11:00 GMT</pubDate>
          <guid>
            http://static.guim.co.uk/audio/kip/science/series/science/1447432633353/5114/gdn.sci.151116.ic.Science_Weekly.mp3
          </guid>
          <itunes:duration>00:27:00</itunes:duration>
          <itunes:author>theguardian.com</itunes:author>
          <itunes:explicit>no</itunes:explicit>
          <itunes:clean>no</itunes:clean>
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
          <enclosure url=""/>
          <pubDate>Fri, 06 Nov 2015 07:30:00 GMT</pubDate>
          <guid>
            http://static.guim.co.uk/audio/kip/science/series/science/1446638390950/3741/gdn.sci.151106.ic.Science_Weekly.mp3
          </guid>
          <itunes:duration>00:25:37</itunes:duration>
          <itunes:author>theguardian.com</itunes:author>
          <itunes:explicit>no</itunes:explicit>
          <itunes:clean>no</itunes:clean>
          <itunes:keywords>Science, David Eagleman, Neuroscience</itunes:keywords>
          <itunes:subtitle>
            Neuroscientist David Eagleman discusses how neuroscience and technology are reshaping how we understand our brains
          </itunes:subtitle>
          <itunes:summary>
            Neuroscientist David Eagleman discusses how neuroscience and technology are reshaping how we understand our brains
          </itunes:summary>
        </item>
      )
    )
  }
}
