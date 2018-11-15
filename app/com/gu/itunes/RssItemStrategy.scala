package com.gu.itunes

import com.gu.contentapi.client.model.v1.Content
import java.time.Instant

trait ItemStrategy {
  def cta(standfirst: String): String
  def guid(content: Content): String
}