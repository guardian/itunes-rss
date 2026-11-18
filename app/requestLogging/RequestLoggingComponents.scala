package requestLogging

import play.api.BuiltInComponents
import play.api.mvc.EssentialFilter
import requestlogging.{ RequestLoggingConfig, RequestLoggingFilter }

trait RequestLoggingComponents extends BuiltInComponents {
  override lazy val httpFilters: Seq[EssentialFilter] =
    Seq(new RequestLoggingFilter(new RequestLoggingConfig(buildCommitId = Some(app.BuildInfo.gitCommitId))))
}
