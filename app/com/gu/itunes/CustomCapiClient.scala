package com.gu.itunes

import java.util.concurrent.TimeUnit
import com.gu.contentapi.client.ContentApiClient
import com.gu.contentapi.client.model.HttpResponse
import java.io.IOException
import okhttp3.{ ConnectionPool, OkHttpClient, Request, Response, Callback, Call }
import play.api.Logger
import scala.concurrent.duration.Duration
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
        if (!resp.isSuccessful)
          response failure (new IOException("Invalid HTTP response: " ++ resp.toString))
        else
          response success HttpResponse(resp.body.bytes, resp.code, resp.message)
    })

    response.future map { result =>
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
