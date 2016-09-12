package controllers


import models._
import play.api.{Routes, Logger}
import play.twirl.api.Html
import securesocial.core.providers.utils.PasswordHasher
import securesocial.core.{RuntimeEnvironment, BasicProfile, AuthenticationMethod}


class Application(override implicit val env: RuntimeEnvironment[UtAdminUser]) extends securesocial.core.SecureSocial[UtAdminUser] {

  val log = Logger(this getClass() getName())

  val server = UtServer

  /** Create initial user if no users exist yet */
  if (UtAdminUserService.getAllUsers.isEmpty) {
    val testUser = new UtAdminUser(
      rank = Ranks.God,
      main = new BasicProfile(providerId = "userpass",
        userId = "admin",
        firstName = Some("Admin"),
        lastName = Some("Admin"),
        fullName = Some("Admin"),
        email = Some("Admin"),
        avatarUrl = None,
        authMethod = AuthenticationMethod.UserPassword,
        oAuth1Info = None,
        oAuth2Info = None,
        passwordInfo = Some(new PasswordHasher.Default().hash("Administrator"))),
      b3Id = 0)
    UtAdminUserService.insertUtAdminUser(testUser)
  }

  /** Gernerate JavaScript Functions for accessing the Services
    * check the corresponding javascript files in the public folder
    */
  def javascriptRoutes = SecuredAction { implicit request =>
    Ok(
      Routes.javascriptRouter("jsRoutes")(
        routes.javascript.Application.index,
        routes.javascript.Rcon.slap,
        routes.javascript.Rcon.say,
        routes.javascript.Rcon.privateMessage,
        routes.javascript.Rcon.kick,
        routes.javascript.Rcon.nuke,
        routes.javascript.Rcon.kill,
        routes.javascript.Rcon.startServerDemo,
        routes.javascript.Rcon.stopServerDemo,
        routes.javascript.Rcon.forceRed,
        routes.javascript.Rcon.forceBlue,
        routes.javascript.Rcon.forceSpec,
        routes.javascript.Rcon.privateMessage,
        routes.javascript.AccountsController.addUser,
        routes.javascript.UserController.changeGroupOfUser,
        routes.javascript.UserController.setXlrVisibility
      )
    ).as("text/javascript")
  }


  def index = SecuredAction {
    request =>
      MongoLogger.logAction(request.user, "index")
      Ok(views.html.index(request.user))
  }
}