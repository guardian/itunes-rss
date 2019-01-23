package com.gu.itunes

import com.gu.contentapi.client.model.v1._
import org.scalatest.{ FlatSpec, Matchers }

class AcastProxySpec extends FlatSpec with Matchers with ItunesTestData {

  val testContent: Seq[Content] = itunesCapiResponseAcastTest.results.get
  val tag: String = itunesCapiResponseAcastTest.tag.get.id

  it should "use the acast proxy" in {
    val newItem: Content = testContent.head
    val asset: Asset = testContent.head.elements.head.head.assets.find(_.`type` == AssetType.Audio).get
    val item = new iTunesRssItem(newItem, tag, asset)
    (item.toXml.head \\ "enclosure" \ "@url").text shouldBe "https://flex.acast.com/audio.guim.co.uk/2017/05/30-51066-gnl.rs.brexit.20170530.thelastbeforethelection.mp3"
  }

  it should "use Guardian origin" in {
    val oldItem: Content = testContent(1)
    val asset: Asset = testContent(1).elements.head.head.assets.find(_.`type` == AssetType.Audio).get
    val item = new iTunesRssItem(oldItem, tag, asset)
    (item.toXml.head \\ "enclosure" \ "@url").text shouldBe "https://audio.guim.co.uk/2017/05/24-54013-gnl.rs.brexit.20170524.negotiationsguidelines.mp3"
  }

}
