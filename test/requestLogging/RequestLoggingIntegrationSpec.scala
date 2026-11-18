package requestLogging

import com.gu.itunes.Application
import controllers.AssetsComponents
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.routing.Router
import play.api.test.TestServer
import play.api.{ ApplicationLoader, BuiltInComponentsFromContext, Environment }
import requestlogging.RequestLoggingFilter
import requestlogging.testkit.RequestLogCapture
import router.Routes

import java.net.URI
import java.net.http.{ HttpClient, HttpRequest, HttpResponse }

/*
 * Boots the app's real routes and controller behind RequestLoggingComponents on a real HTTP
 * server, and checks what the request logging filter logs using the requestlogging test-kit.
 *
 * AppComponents itself isn't booted because it needs AWS (SSM) for secret rotation.
 */
class RequestLoggingIntegrationSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {

  private class TestComponents(context: ApplicationLoader.Context)
    extends BuiltInComponentsFromContext(context)
    with RequestLoggingComponents
    with AssetsComponents {
    lazy val appController = new Application(controllerComponents, configuration)
    lazy val router: Router = new Routes(httpErrorHandler, appController, assets)
  }

  private val context = ApplicationLoader.Context.create(
    environment = Environment.simple(),
    initialSettings = Map("apiKey" -> "test-api-key", "fastlyImageResizerSignatureSalt" -> "test-salt"))

  private val components = new TestComponents(context)
  private val server = TestServer(port = 0, application = components.application)

  private val httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build()

  private var logs: RequestLogCapture = _

  override def beforeAll(): Unit = server.start()
  override def afterAll(): Unit = server.stop()

  override def beforeEach(): Unit = logs = RequestLogCapture.start()
  override def afterEach(): Unit = logs.close()

  // The filter logs before its result completes, so the entry exists once the response arrives.
  private def get(path: String, headers: (String, String)*): HttpResponse[String] = {
    val port = server.runningHttpPort.getOrElse(fail("Test server is not running"))
    val builder = HttpRequest.newBuilder(URI.create(s"http://localhost:$port$path"))
    headers.foreach { case (name, value) => builder.header(name, value) }
    httpClient.send(builder.GET().build(), HttpResponse.BodyHandlers.ofString())
  }

  "AppComponents" should "use RequestLoggingComponents for its HTTP filters" in {
    // AppComponents is in the default package, so can't be referenced directly from here.
    classOf[RequestLoggingComponents].isAssignableFrom(Class.forName("AppComponents")) shouldBe true
  }

  "RequestLoggingComponents" should "install the request logging filter" in {
    components.httpFilters.map(_.getClass) shouldBe Seq(classOf[RequestLoggingFilter])
  }

  it should "log a structured entry for a routed request" in {
    val response = get(
      "/film/series/filmweekly/podcast.xml",
      "User-Agent" -> "integration-test-agent",
      "X-Forwarded-For" -> "203.0.113.1, 10.0.0.1")

    response.statusCode shouldBe 301

    val Seq(entry) = logs.entries
    entry.message shouldBe "HTTP request received"
    entry.httpMethod shouldBe "GET"
    entry.httpStatus shouldBe Some(301)
    entry.route shouldBe "/$tagId<.+>/podcast.xml"
    entry.origin shouldBe "203.0.113.1, 10.0.0.1"
    entry.userAgent shouldBe "integration-test-agent"
    entry.buildCommit shouldBe Some(app.BuildInfo.gitCommitId)
    entry.requestId should not be empty
    entry.durationMs should be >= 0L
    entry.exception shouldBe None
  }

  it should "fall back to the remote address when there is no X-Forwarded-For header" in {
    get("/film/series/filmweekly/podcast.xml")

    logs.entries.map(_.origin) should (equal(Seq("127.0.0.1")) or equal(Seq("0:0:0:0:0:0:0:1")))
  }

  it should "log unmatched routes" in {
    val response = get("/no/such/route")

    response.statusCode shouldBe 404
    logs.entries.map(entry => (entry.route, entry.httpStatus)) shouldBe Seq(("<unmatched>", Some(404)))
  }

  it should "not log healthcheck requests" in {
    val response = get("/healthcheck")

    response.statusCode shouldBe 200
    response.body shouldBe "OK"
    logs.entries shouldBe empty
  }

  it should "not log asset requests" in {
    get("/assets/does-not-exist.css")

    logs.entries shouldBe empty
  }
}
