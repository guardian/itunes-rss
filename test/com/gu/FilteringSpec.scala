package com.gu.itunes

import org.scalatest.{ Matchers, FlatSpec }

class FilteringSpec extends FlatSpec with Matchers {

  behavior of "standfirst"

  it should "extract content inside <p>" in {

    val source = "<p>Only 7% of American women keep their last names when marrying. Columnist Jessica Valenti and guest Laurie Scheuble discuss why that is</p>"
    val filtered = Filtering.standfirst(source)

    val expected = "Only 7% of American women keep their last names when marrying. Columnist Jessica Valenti and guest Laurie Scheuble discuss why that is"

    filtered should be(expected)
  }

  behavior of "description"

  it should "remove <br> and extract content inside <p>" in {

    val source = """<p>The Guardian’s&nbsp;<a href=\"http://www.theguardian.com/profile/jessicavalenti\">Jessica Valenti</a>&nbsp;brings you interviews, advice and real life stories from the front lines of feminism. She talks about everything from periods or lack there of, to cyberbullying, objectification, crafting an equal and egalitarian relationship, abortions and sex education. This is the place for women and men to share their questions and thoughts about everyday issues facing women and feminism. Want to ask a question? Leave us a voicemail:&nbsp;<a href=\"tel:917-900-4577\">917-900-4577</a><br></p>"""

    val filtered = Filtering.description(source)

    val expected = """The Guardian’s  Jessica Valenti   brings you interviews, advice and real life stories from the front lines of feminism. She talks about everything from periods or lack there of, to cyberbullying, objectification, crafting an equal and egalitarian relationship, abortions and sex education. This is the place for women and men to share their questions and thoughts about everyday issues facing women and feminism. Want to ask a question? Leave us a voicemail:  917-900-4577"""

    filtered should be(expected)

  }

}