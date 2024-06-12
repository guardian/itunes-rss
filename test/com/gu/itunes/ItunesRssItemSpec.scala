package com.gu.itunes

import com.gu.itunes.XmlTestUtils.RemoveWhitespace
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.xml.Utility.trim
import org.scalatest.OptionValues

class ItunesRssItemSpec extends AnyFlatSpec with ItunesTestData with Matchers with OptionValues {

  val imageResizerSalt: Option[String] = Some("abcdefabcdefabcdef")

  it should "check that the produced XML for the podcasts is consistent" in {

    val results = itunesCapiResponse.results.getOrElse(Nil)
    val tagId = itunesCapiResponse.tag.get.id
    val podcasts = for (p <- results) yield new iTunesRssItem(p, tagId, p.elements.get.head.assets.head, adFree = false, imageResizerSignatureSalt = imageResizerSalt).toXml
    val trimmedPodcasts = for (p <- podcasts) yield trim(p)

    val expectedXml = RemoveWhitespace.transform(
      <item>
        <title>
          Inside the mind of renowned mathematician John Conway
        </title>
        <itunes:title>
          Inside the mind of renowned mathematician John Conway
        </itunes:title>
        <description>
          John Conway sheds light on the true nature of numbers, the beauty lying within maths and why game-playing is so important to mathematical discovery
        </description>
        <enclosure url="http://static.guim.co.uk/audio/kip/science/series/science/1447948283860/6835/gdn.sci.151120.ic.Science_Weekly_2.mp3" length="0" type="audio/mpeg"/>
        <pubDate>Sun, 20 Jan 2019 07:30:00 GMT</pubDate>
        <guid isPermaLink="false">
          composer-code-123
        </guid>
        <itunes:duration>00:29:07</itunes:duration>
        <itunes:author>The Guardian</itunes:author>
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
        <itunes:title>Why are conspiracy theories so attractive? podcast</itunes:title>
        <description>
          Should we distrust our own ability to reason? Why is debunking conspiracy theories such a risky business? And is David Icke a force for good?
        </description>
        <enclosure url="http://static.guim.co.uk/audio/kip/science/series/science/1447432633353/5114/gdn.sci.151116.ic.Science_Weekly.mp3" length="0" type="audio/mpeg"/>
        <pubDate>Fri, 04 Dec 2015 11:04:27 GMT</pubDate>
        <guid isPermaLink="true">
          https://audio.guim.co.uk/2015/12/03-53462-gdn.tech.151203.sb.digital-babysitting.mp3
        </guid>
        <itunes:duration>00:27:00</itunes:duration>
        <itunes:author>The Guardian</itunes:author>
        <itunes:explicit>yes</itunes:explicit>
        <itunes:keywords>Science, Psychology</itunes:keywords>
        <itunes:subtitle>
          Should we distrust our own ability to reason? Why is debunking conspiracy theories such a risky business? And is David Icke a force for good?
        </itunes:subtitle>
        <itunes:summary>
          Should we distrust our own ability to reason? Why is debunking conspiracy theories such a risky business? And is David Icke a force for good?
        </itunes:summary>
      </item>
      <item>
        <title>The story of our brains</title>
        <itunes:title>The story of our brains</itunes:title>
        <description>
          Neuroscientist David Eagleman discusses how neuroscience and technology are reshaping how we understand our brains
        </description>
        <enclosure url="http://static.guim.co.uk/audio/kip/science/series/science/1446638390950/3741/gdn.sci.151106.ic.Science_Weekly.mp3" length="0" type="audio/mpeg"/>
        <pubDate>Fri, 06 Nov 2015 07:30:00 GMT</pubDate>
        <guid isPermaLink="true">
          http://download.guardian.co.uk/draft/audio/1446638390950/3741/gdn.sci.151106.ic.Science_Weekly.mp3
        </guid>
        <itunes:duration>00:25:37</itunes:duration>
        <itunes:author>The Guardian</itunes:author>
        <itunes:explicit>clean</itunes:explicit>
        <itunes:keywords>Science, David Eagleman, Neuroscience</itunes:keywords>
        <itunes:subtitle>
          Neuroscientist David Eagleman discusses how neuroscience and technology are reshaping how we understand our brains
        </itunes:subtitle>
        <itunes:summary>
          Neuroscientist David Eagleman discusses how neuroscience and technology are reshaping how we understand our brains
        </itunes:summary>
      </item>)

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

  it should "omit itunes:subtitle tag from ad free feeds as it is often out of spec" in {
    // Often exceeds 255 characters which is reported as a validation failure in the w3c feed validation tool.
    // Omit from ad free feeds to avoid validation rejections.
    val results = itunesCapiResponse.results.getOrElse(Nil)
    val tagId = itunesCapiResponse.tag.get.id

    val podcasts = for (p <- results) yield new iTunesRssItem(p, tagId, p.elements.get.head.assets.head, true, imageResizerSignatureSalt = imageResizerSalt).toXml

    val firstItemSubtitleTag = (podcasts \\ "item" \ "subtitle").find(_.prefix == "itunes")
    firstItemSubtitleTag should be(None)
  }

  it should "use shorter trailtext for itunes:subtitle to help stay within the w3c 255 character limit" in {
    val results = itunesCapiResponse.results.getOrElse(Nil)
    val tagId = itunesCapiResponse.tag.get.id

    val podcasts = for (p <- results) yield new iTunesRssItem(p, tagId, p.elements.get.head.assets.head, false, imageResizerSignatureSalt = imageResizerSalt).toXml

    val itemSubtitleTags = (podcasts \\ "item" \ "subtitle").filter(_.prefix == "itunes")
    itemSubtitleTags.lastOption.map(_.text) should be(Some("Guardian Australia editor Lenore Taylor and head of news Mike Ticher discuss the expansion of Covid financial support in NSW"))
  }

  it should "include itunes:episode tag for serial podcast with episode in web title" in {
    val tag = itunesCapiResponseEpisodeNumber.tag.get
    tag.podcast.value.podcastType.value should be("serial")
    val result = itunesCapiResponseEpisodeNumber.results.get.head
    val rssItem = new iTunesRssItem(result, tag.id, result.elements.get.head.assets.head, false,
      tag.podcast.value.podcastType, imageResizerSignatureSalt = imageResizerSalt).toXml
    val episodeTag = (rssItem \ "episode").head
    episodeTag.prefix should be("itunes")
    episodeTag.text should be("6")
  }

  it should "not set itunes:episode tag when not a serial podcast" in {
    // this input contains "Episode X" in the title but it should be
    // ignored because it isn't a `itunes:type = "serial"` podcast.
    val tag = itunesCapiResponseNoType.tag.get
    val result = itunesCapiResponseEpisodeNumber.results.get.head
    val rssItem = new iTunesRssItem(result, tag.id, result.elements.get.head.assets.head, false,
      tag.podcast.value.podcastType, imageResizerSignatureSalt = imageResizerSalt).toXml
    (rssItem \ "episode") shouldBe empty
  }

  it should "not include itunes:episode tag when episode marker not included even if serial type" in {
    val tag = itunesCapiResponse.tag.get
    val result = itunesCapiResponse.results.get.head
    val rssItem = new iTunesRssItem(result, tag.id, result.elements.get.head.assets.head, false,
      tag.podcast.value.podcastType, imageResizerSignatureSalt = imageResizerSalt).toXml
    (rssItem \ "episode") shouldBe empty
  }

  it should "add episodic artwork where appropriate" in {
    val tag = itunesCapiResponseComfortEating.tag.get
    val resultWithEpisodicImage = itunesCapiResponseComfortEating.results.get.head
    val rssItemWithEpisodicImage = new iTunesRssItem(resultWithEpisodicImage, tag.id, resultWithEpisodicImage.elements.get.head.assets.head, false,
      tag.podcast.value.podcastType, imageResizerSignatureSalt = imageResizerSalt).toXml
    // make sure we have the expected item!
    val expectedGuid = "66168c368f085cc6169111c5"
    val actualGuid = (rssItemWithEpisodicImage \ "guid").head.text
    actualGuid shouldBe (expectedGuid)
    // now check our image exists
    val itunesImages = (rssItemWithEpisodicImage \\ "image")
    itunesImages.size shouldBe (1)
    val expectedImage = """<itunes:image href="https://i.guim.co.uk/img/media/39f24967bf11d6a8298201d29e7f7a3b7e0517b8/0_0_2999_1800/2999.jpg?width=3000&amp;height=3000&amp;quality=75&amp;fit=crop&amp;s=c14d895fa1e69eeff7948fe06f7c4c01"/>"""
    itunesImages.head.toString() shouldBe (expectedImage)
  }

  it should "not add episodic artwork where not appropriate" in {
    val tag = itunesCapiResponseComfortEating.tag.get
    val content = itunesCapiResponseComfortEating.results.map(_.drop(3).head).head
    val rssItem = new iTunesRssItem(content, tag.id, content.elements.get.head.assets.head, false,
      tag.podcast.value.podcastType, imageResizerSignatureSalt = imageResizerSalt).toXml
    // make sure we have the expected item!
    val expectedGuid = "65fd51098f0872bcdbfc1d5d"
    val actualGuid = (rssItem \ "guid").head.text
    actualGuid shouldBe (expectedGuid)
    val itunesImages = (rssItem \\ "image")
    itunesImages.size shouldBe (0)
  }

  it should "not add episodic artwork if there is no fastly salt defined" in {
    val tag = itunesCapiResponseComfortEating.tag.get
    val noneResizerSalt: Option[String] = None
    val resultWithEpisodicImage = itunesCapiResponseComfortEating.results.get.head
    val rssItemWithEpisodicImage = new iTunesRssItem(resultWithEpisodicImage, tag.id, resultWithEpisodicImage.elements.get.head.assets.head, false,
      tag.podcast.value.podcastType, imageResizerSignatureSalt = noneResizerSalt).toXml
    // make sure we have the expected item!
    val expectedGuid = "66168c368f085cc6169111c5"
    val actualGuid = (rssItemWithEpisodicImage \ "guid").head.text
    actualGuid shouldBe (expectedGuid)
    // now check our image exists
    val itunesImages = (rssItemWithEpisodicImage \\ "image")
    itunesImages.size shouldBe (0)
  }

  it should "not add episodic artwork if the fastly salt is defined as an empty string" in {
    // this shouldn't happen naturally but if iTunesRssItem.episodeImage is ever changed
    // and allows the salt param through as an empty string this test should start failing
    val tag = itunesCapiResponseComfortEating.tag.get
    val emptyResizerSalt: Option[String] = Some("")
    val resultWithEpisodicImage = itunesCapiResponseComfortEating.results.get.head
    val rssItemWithEpisodicImage = new iTunesRssItem(resultWithEpisodicImage, tag.id, resultWithEpisodicImage.elements.get.head.assets.head, false,
      tag.podcast.value.podcastType, imageResizerSignatureSalt = emptyResizerSalt).toXml
    // make sure we have the expected item!
    val expectedGuid = "66168c368f085cc6169111c5"
    val actualGuid = (rssItemWithEpisodicImage \ "guid").head.text
    actualGuid shouldBe (expectedGuid)
    // now check our image exists
    val itunesImages = (rssItemWithEpisodicImage \\ "image")
    itunesImages.size shouldBe (0)
  }

}
