package com.gu.itunes

import scala.xml.transform.{ RewriteRule, RuleTransformer }
import scala.xml.{ XML, Elem, Node, Text }

object XmlTestUtils {

  object RemoveWhitespaceRule extends RewriteRule {
    override def transform(ns: Seq[Node]): Seq[Node] = ns.collect {
      case Text(t) if t.trim.isEmpty => None
      case Text(t) => Some(Text(t.trim))
      case other => Some(other)
    }.flatten
  }

  object RemoveWhitespace extends RuleTransformer(RemoveWhitespaceRule)

  /**
   * Extract the contents of an RSS item's <content:encoded> tag as XML.
   * @param itemXml the RSS item as XML
   */
  def parseContentHtml(itemXml: Elem): Elem = {
    val text = (itemXml \ "encoded").head.text
    val withoutDoctype = text.lines.drop(1).mkString("\n")
    XML.loadString(withoutDoctype)
  }

  def cleanXml(xml: Node): Elem = {
    // Convert the XML to a string and back to get rid of any `Group` instances.
    // `Group` is a nasty hack and it doesn't support traversal.
    XML.loadString(xml.toString())
  }

}
