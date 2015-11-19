package com.gu.itunes

import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model.{ SearchQuery, SearchResponse }
import com.gu.contentapi.client.parser.JsonParser
import dispatch.Http
import play.api.Logger

import scala.concurrent.duration.Duration
import scala.concurrent.{ ExecutionContext, Future }

class CustomCapiClient(apiKey: String) extends GuardianContentClient(apiKey) {

  // Use the same HTTP client for the whole lifecycle of the Play app,
  // rather than creating a new one per request
  override protected lazy val http = CustomCapiClient.http

  override protected def fetch(url: String)(implicit context: ExecutionContext): Future[String] = {
    val start = System.nanoTime()
    val future = super.fetch(url)(context)
    future map { result =>
      val end = System.nanoTime()
      Logger.info(s"Received CAPI response in ${Duration.fromNanos(end - start).toMillis} ms")
      result
    }
  }

  override def getResponse(searchQuery: SearchQuery)(implicit context: ExecutionContext): Future[SearchResponse] = {
    fetch(getUrl(searchQuery)) map { string =>
      val start = System.nanoTime()
      val result = JsonParser.parseSearch(string)
      val end = System.nanoTime()
      Logger.info(s"Parsed JSON in ${Duration.fromNanos(end - start).toMillis} ms")
      result
    }
  }

}

object CustomCapiClient {

  val http = Http configure {
    _
      .setAllowPoolingConnections(true)
      .setMaxConnectionsPerHost(10)
      .setMaxConnections(10)
      .setConnectTimeout(10000)
      .setRequestTimeout(10000)
      .setCompressionEnforced(true)
      .setFollowRedirect(true)
      .setConnectionTTL(60000) // to respect DNS TTLs
  }

}
