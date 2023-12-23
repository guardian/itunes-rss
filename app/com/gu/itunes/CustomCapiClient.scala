package com.gu.itunes

import com.gu.contentapi.client.HttpRetry.withRetry
import com.gu.contentapi.client.model.HttpResponse
import com.gu.contentapi.client.{BackoffStrategy, ContentApiClient, ScheduledExecutor}
import okhttp3._
import org.slf4j.LoggerFactory

import java.io.IOException
import java.util.concurrent.TimeUnit
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}

class CustomCapiClient(val apiKey: String) extends ContentApiClient {
  private val logger = LoggerFactory.getLogger(getClass)

  implicit val executor: ScheduledExecutor = ScheduledExecutor()
  private val initialDelay = 1000.millis
  private val backoffStrategy: BackoffStrategy = BackoffStrategy.multiplierStrategy(initialDelay, multiplier = 1.5, maxAttempts = 3)

  // Use the same HTTP client for the whole lifecycle of the Play app,
  // rather than creating a new one per request
  lazy val http = CustomCapiClient.http

  override def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext): Future[HttpResponse] = withRetry(backoffStrategy) { retryAttempt =>
    val start = System.nanoTime()
    val updatedHeaders = headers + ("Request-Attempt" -> s"$retryAttempt")
    val request = updatedHeaders.foldLeft(new Request.Builder().url(url)) { case (r, (header, value)) => r.header(header, value) }.build()
    val response = Promise[HttpResponse]()
    http.newCall(request).enqueue(new Callback {
      override def onFailure(call: Call, e: IOException) = response failure e

      override def onResponse(call: Call, resp: Response) =
        response success HttpResponse(resp.body.bytes, resp.code, resp.message)
    })

    response.future map { result =>
      val end = System.nanoTime()
      logger.info(s"Received CAPI response ${result.statusCode} in ${Duration.fromNanos(end - start).toMillis} ms")
      result
    }
  }
}

object CustomCapiClient {

  val http = new OkHttpClient.Builder()
    .connectTimeout(1, TimeUnit.SECONDS)
    .readTimeout(2, TimeUnit.SECONDS)
    .followRedirects(true)
    .connectionPool(new ConnectionPool(10, 60, TimeUnit.SECONDS))
    .build()

}
