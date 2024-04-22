package com.gu.itunes

import org.joda.time.{ DateTime, DateTimeZone }
import org.joda.time.format.DateTimeFormat

object DateSupport {
  val dateFormatter = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss")

  def toRssTimeFormat(dateTime: DateTime) = {
    dateTime.withZone(DateTimeZone.UTC).toString(dateFormatter) + " GMT"
  }
}

