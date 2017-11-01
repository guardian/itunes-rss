package com.gu.itunes

import java.util.concurrent.TimeUnit
import com.gu.contentapi.client.GuardianContentClient
import okhttp3.{ ConnectionPool, OkHttpClient }
import play.api.Logger
import scala.concurrent.duration.Duration
import scala.concurrent.{ ExecutionContext, Future }

class CustomCapiClient(apiKey: String) extends GuardianContentClient(apiKey) {

  // Use the same HTTP client for the whole lifecycle of the Play app,
  // rather than creating a new one per request
  override protected lazy val http = CustomCapiClient.http

  override protected def fetch(url: String)(implicit context: ExecutionContext): Future[Array[Byte]] = {
    val start = System.nanoTime()
    val future = super.fetch(url)(context)
    future map { result =>
      val end = System.nanoTime()
      Logger.info(s"Received CAPI response in ${Duration.fromNanos(end - start).toMillis} ms")
      result
    }
  }

}

object CustomCapiClient {

  val http = new OkHttpClient.Builder()
    .connectTimeout(1000, TimeUnit.SECONDS)
    .readTimeout(2000, TimeUnit.SECONDS)
    .followRedirects(true)
    .connectionPool(new ConnectionPool(10, 60, TimeUnit.SECONDS))
    .build()

}
