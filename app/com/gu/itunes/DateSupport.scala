package com.gu.itunes

import org.joda.time.{ DateTimeZone, DateTime }
import org.joda.time.format.DateTimeFormat

object DateSupport {
  val dateFormatter = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss")

  def toRssTimeFormat(someTime: DateTime) = {
    someTime.withZone(DateTimeZone.UTC).toString(dateFormatter) + " GMT"
  }
}

