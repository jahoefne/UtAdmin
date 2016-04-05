import java.lang.reflect.Constructor
import play.api.mvc._
import play.filters.gzip.GzipFilter
import play.twirl.api.Html
import securesocial.core._
import securesocial.core.providers.UsernamePasswordProvider
import models.{UtAdminUserService, UtAdminUser}
import scala.collection.immutable.ListMap
import play.api._
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.Future


object Global extends WithFilters(new GzipFilter()) with play.api.GlobalSettings {
  /**
   * The runtime environment for this sample app.
   */
  object MyRuntimeEnvironment extends RuntimeEnvironment.Default[UtAdminUser] {
    override lazy val userService = UtAdminUserService
    override lazy val providers = ListMap(include(new UsernamePasswordProvider(
      userService,
      None,
      viewTemplates,
      passwordHashers))
    )
  }

  /**
   * An implementation that checks if the controller expects a RuntimeEnvironment and
   * passes the instance to it if required.
   *
   * This can be replaced by any DI framework to inject it differently.
   *
   * @param controllerClass
   * @tparam A
   * @return
   */
  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    val instance = controllerClass.getConstructors.find { c =>
      val params = c.getParameterTypes
      params.length == 1 && params(0) == classOf[RuntimeEnvironment[UtAdminUser]]
    }.map {
      _.asInstanceOf[Constructor[A]].newInstance(MyRuntimeEnvironment)
    }
    instance.getOrElse(super.getControllerInstance(controllerClass))
  }


  // called when a route is found, but it was not possible to bind the request parameters
  override def onBadRequest(request: RequestHeader, error: String) = Future.successful{
    BadRequest(
      views.html.styledError(
        Html(
          """
            <div align="center" style="padding:50px;">
            <image src="http://i.imgur.com/MMmmegA.jpg"></image>
            </div>
          """),
        None)
    )
  }

  // 500 - internal server error
  override def onError(request: RequestHeader, throwable: Throwable) = Future.successful{
    InternalServerError(
      views.html.styledError(
        Html(
          """
            <div align="center" style="padding:50px;">
            <image src="http://i.imgur.com/9PgKgLL.jpg"></image>
            </div>
          """),
        None)
    )
  }

  // 404 - page not found error
  override def onHandlerNotFound(request: RequestHeader) = Future.successful{
    NotFound(      views.html.styledError(
      Html(
        """
            <div align="center" style="padding:50px;">
            <image src="http://i.imgur.com/SkkGRnD.jpg"></image>
            </div>
        """),
      None
    ))
  }
}