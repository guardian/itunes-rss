import com.gu.itunes.{ Application, SecretKeeper }
import com.gu.play.secretrotation.{ RotatingSecretComponents, SnapshotProvider, TransitionTiming }
import controllers.AssetsComponents
import play.api.ApplicationLoader.Context
import play.api.{ BuiltInComponentsFromContext, NoHttpFiltersComponents }
import play.api.routing.Router
import router.Routes
import software.amazon.awssdk.services.ssm.SsmClient

import java.time.Duration.{ ofHours, ofMinutes }
/* if the import router.Routes above says that it can't resolve router,
   this article was useful: https://stackoverflow.com/questions/37333140/cannot-import-class-router-routes-in-applicationloader-on-play-2-5
   tl;dr
   1. Make sure your build.sbt contains routesGenerator := InjectedRoutesGenerator
   2. Execute playCompileEverything in sbt and refresh your project in your IDE
*/

class AppComponents(context: Context)
  extends BuiltInComponentsFromContext(context)
  with NoHttpFiltersComponents
  with AssetsComponents
  with RotatingSecretComponents {

  val secretStateSupplier: SnapshotProvider = {
    import com.gu.play.secretrotation.aws.parameterstore

    new parameterstore.SecretSupplier(
      TransitionTiming(usageDelay = ofMinutes(3), overlapDuration = ofHours(2)),
      "/ANY/content-api/podcasts-rss/play-secret",
      parameterstore.AwsSdkV2(SsmClient.builder().credentialsProvider(SecretKeeper.credentialsProviderChain).build()))
  }

  val appController = new Application(controllerComponents, configuration)
  val router: Router = new Routes(httpErrorHandler, appController, assets)

}