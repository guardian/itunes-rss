package com.gu.itunes

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class FilteringSpec extends AnyFlatSpec with Matchers {

  behavior of "standfirst"

  it should "extract content inside <p>" in {
    val source = "<p>Only 7% of American women keep their last names when marrying. Columnist Jessica Valenti and guest Laurie Scheuble discuss why that is</p>"
    val filtered = Filtering.standfirst(source, preserveHtml = false)
    val expected = "Only 7% of American women keep their last names when marrying. Columnist Jessica Valenti and guest Laurie Scheuble discuss why that is"
    filtered should be(expected)
  }

  it should "retain a tags with href attribute and enforce rel=nofollow within standFirst's <p> when instructed" in {
    val source = "<p>This is an introductory paragraph.</p><p>• Here we link to <a href=\"https://www.theguardian.com/news/audio/2023/apr/24/embracing-a-childfree-life-podcast\">another episode</a> from April 2023, and link to an article that is related to this episode <a href=\"https://www.theguardian.com/lifeandstyle/2024/nov/16/friendship-after-motherhood\">here</a>.</p>"
    val filtered = Filtering.standfirst(source, preserveHtml = true)
    val expected = "This is an introductory paragraph. • Here we link to <a href=\"https://www.theguardian.com/news/audio/2023/apr/24/embracing-a-childfree-life-podcast\" rel=\"nofollow\">another episode</a> from April 2023, and link to an article that is related to this episode <a href=\"https://www.theguardian.com/lifeandstyle/2024/nov/16/friendship-after-motherhood\" rel=\"nofollow\">here</a>."
    filtered should be(expected)
  }

  it should "retain all tags defined by Safelist.simpleText within standFirst's <p> when instructed" in {
    val source = "<p>This is a <strong>strong introductory</strong> paragraph.</p><p>• Here we <b>boldly</b> link to <a href=\"https://www.theguardian.com/news/audio/2023/apr/24/embracing-a-childfree-life-podcast\">another episode</a> from April 2023, and <em>emphasise this link</em> to an <i>italicised</i> article that is related to and <u>underscores</u> this episode <a href=\"https://www.theguardian.com/lifeandstyle/2024/nov/16/friendship-after-motherhood\">here</a>.</p>"
    val filtered = Filtering.standfirst(source, preserveHtml = true)
    val expected = "This is a <strong>strong introductory</strong> paragraph. • Here we <b>boldly</b> link to <a href=\"https://www.theguardian.com/news/audio/2023/apr/24/embracing-a-childfree-life-podcast\" rel=\"nofollow\">another episode</a> from April 2023, and <em>emphasise this link</em> to an <i>italicised</i> article that is related to and <u>underscores</u> this episode <a href=\"https://www.theguardian.com/lifeandstyle/2024/nov/16/friendship-after-motherhood\" rel=\"nofollow\">here</a>."
    filtered should be(expected)
  }

  it should "remove all html from the standFirst <p> when instructed" in {
    val source = "<p>This is a <strong>strong introductory</strong> paragraph.</p><p>• Here we <b>boldly</b> link to <a href=\"https://www.theguardian.com/news/audio/2023/apr/24/embracing-a-childfree-life-podcast\">another episode</a> from April 2023, and <em>emphasise this link</em> to an <i>italicised</i> article that is related to and <u>underscores</u> this episode <a href=\"https://www.theguardian.com/lifeandstyle/2024/nov/16/friendship-after-motherhood\">here</a>.</p>"
    val filtered = Filtering.standfirst(source, preserveHtml = false)
    val expected = "This is a strong introductory paragraph. • Here we boldly link to another episode from April 2023, and emphasise this link to an italicised article that is related to and underscores this episode here."
    filtered should be(expected)
  }

  behavior of "description"

  it should "remove <br>, extract content inside <p> and remove <a> tags but retain text from within" in {
    val source = """<p>The Guardian’s&nbsp;<a href=\"http://www.theguardian.com/profile/jessicavalenti\">Jessica Valenti</a>&nbsp;brings you interviews, advice and real life stories from the front lines of feminism. She talks about everything from periods or lack there of, to cyberbullying, objectification, crafting an equal and egalitarian relationship, abortions and sex education. This is the place for women and men to share their questions and thoughts about everyday issues facing women and feminism. Want to ask a question? Leave us a voicemail:&nbsp;<a href=\"tel:917-900-4577\">917-900-4577</a><br></p>"""
    val filtered = Filtering.description(source)
    val expected = """The Guardian’s Jessica Valenti brings you interviews, advice and real life stories from the front lines of feminism. She talks about everything from periods or lack there of, to cyberbullying, objectification, crafting an equal and egalitarian relationship, abortions and sex education. This is the place for women and men to share their questions and thoughts about everyday issues facing women and feminism. Want to ask a question? Leave us a voicemail: 917-900-4577"""
    filtered should be(expected)
  }

}