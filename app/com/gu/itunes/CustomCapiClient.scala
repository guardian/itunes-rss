package com.gu.itunes

import java.util.concurrent.TimeUnit

import com.gu.contentapi.client.{ ContentApiBackoff, ContentApiClient, ScheduledExecutor }
import com.gu.contentapi.client.model.HttpResponse
import java.io.IOException

import okhttp3.{ Call, Callback, ConnectionPool, OkHttpClient, Request, Response }
import play.api.Logger

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future, Promise }

class CustomCapiClient(val apiKey: String) extends ContentApiClient {

  // Use the same HTTP client for the whole lifecycle of the Play app,
  // rather than creating a new one per request
  lazy val http = CustomCapiClient.http

  def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext): Future[HttpResponse] = {
    val start = System.nanoTime()
    val request = headers.foldLeft(new Request.Builder().url(url)) { case (r, (header, value)) => r.header(header, value) }.build()
    val response = Promise[HttpResponse]()
    http.newCall(request).enqueue(new Callback {
      override def onFailure(call: Call, e: IOException) = response failure e

      override def onResponse(call: Call, resp: Response) =
        response success HttpResponse(resp.body.bytes, resp.code, resp.message)
    })

    response.future map { result =>
      val end = System.nanoTime()
      Logger.info(s"Received CAPI response ${result.statusCode} in ${Duration.fromNanos(end - start).toMillis} ms")
      result
    }
  }

  override implicit val executor: ScheduledExecutor = ScheduledExecutor()
  private val initialDelay = 1000.millis
  override val backoffStrategy: ContentApiBackoff = ContentApiBackoff.multiplierStrategy(initialDelay, multiplier = 1.5, maxAttempts = 3)
}

object CustomCapiClient {

  val http = new OkHttpClient.Builder()
    .connectTimeout(1000, TimeUnit.SECONDS)
    .readTimeout(2000, TimeUnit.SECONDS)
    .followRedirects(true)
    .connectionPool(new ConnectionPool(10, 60, TimeUnit.SECONDS))
    .build()

}
