import com.gu.itunes.Application
import controllers.AssetsComponents
import play.api.ApplicationLoader.Context
import play.api.{ BuiltInComponentsFromContext, NoHttpFiltersComponents }
import play.api.routing.Router
import router.Routes

class AppComponents(context: Context)
  extends BuiltInComponentsFromContext(context)
  with NoHttpFiltersComponents
  with AssetsComponents {

  val appController = new Application(controllerComponents, configuration)
  val router: Router = new Routes(httpErrorHandler, appController, assets)

}