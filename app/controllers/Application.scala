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
        routes.javascript.Rcon.forceRed,
        routes.javascript.Rcon.forceBlue,
        routes.javascript.Rcon.forceSpec,
        routes.javascript.Rcon.privateMessage,
        routes.javascript.Application.chatLogPlain,
        routes.javascript.Administrator.addUser
      )
    ).as("text/javascript")
  }


  def index(count: Int, offset: Int) = SecuredAction {
    request =>

      val c = count match {
        case x if x > 0 => count
        case _ => 10
      }
      val o = offset match {
        case x if x >= 0 => x
        case _ => 0
      }
      MongoLogger.logAction(request.user, "index")
      Ok(views.html.status(ChatMessage.getChatLog(server.b3.connection, c, o, None, false), c, o, request.user))
  }

  def onlinePlayersPlain() = SecuredAction {
    request =>
      Ok(views.html.plainOnlinePlayers(B3UserController.listOnlineUsers(server.b3.connection).sortWith(
        (a, b) => {
          a.team > b.team || a.team == b.team && a.score > b.score
        }), request.user))
  }

  def chatLogPlain(count: Int, offset: Int, user: Option[Int], includeRadio: Boolean, showJumpToConv: Boolean) = SecuredAction { request =>
    val c = count match {
      case x if x > 0 => count
      case _ => 10
    }
    val o = offset match {
      case x if x >= 0 => x
      case _ => 0
    }
    val chatlog = ChatMessage.getChatLog(server.b3.connection, c, o, user, includeRadio)

    MongoLogger.logAction(request.user, "Chatlog")
    user match {
      case None => Ok(views.html.chatlog(chatlog, c, o, true, false, showJumpToConv))
      case _ => Ok(views.html.chatlog(chatlog, c, o, relativeTime = false, true, showJumpToConv))
    }
  }

  def userById(id: Int) = SecuredAction { request =>
    User.UserInfo.getUserByB3Id(server.b3.connection, id) match {
      case Some(user) =>
        val userHistory = User.UserInfo.getOnlineHistory(server.b3.connection, user.guid)
        MongoLogger.logAction(request.user, "Userinfo for" + user.currentName + " " + user.guid)
        Ok(views.html.user(user, userHistory, request.user))
      case None =>
        Ok(views.html.styledError(Html("The User you are looking for could not be found!"), Some(request.user)))
    }
  }

  def userlist(count: Int,
               offset: Int,
               minLevel: Int,
               title: String,
               displaySearchField: Boolean = true) = SecuredAction { request =>
    MongoLogger.logAction(request.user, "Userlist")
    val c = count match {
      case x if x > 0 => count
      case _ => 10
    }
    val o = offset match {
      case x if x >= 0 => x
      case _ => 0
    }
    Ok(views.html.userlist(
      users = B3UserController.getUsers(
        conn = server.b3.connection,
        count = c,
        offset = o,
        minLevel = minLevel),
      offsetCount = Some((o, c)),
      title = title,
      displaySearchField = displaySearchField,
      loggedInUser = request.user))
  }

  def userSearch(name: String) = SecuredAction { request =>
    MongoLogger.logAction(request.user, "Searching for user: " + name)
    Ok(views.html.usersearch(User.UserSearch.findUser(server.b3.connection, name), request.user))
  }

  def chatLog(count: Int, offset: Int) = SecuredAction { request =>
    val c = count match {
      case x if x > 0 => count
      case _ => 30
    }
    val o = offset match {
      case x if x >= 0 => x
      case _ => 0
    }
    MongoLogger.logAction(request.user, "ChatlogWrapper")
    val chatlog = ChatMessage.getChatLog(server.b3.connection, c, o, None, false)

    Ok(views.html.chatlogWrapper(chatlog, c, o, request.user))
  }

  def chatLogForId(msgId: Int) = SecuredAction { request =>
    val offset = ChatMessage.getOffsetFor(server.b3.connection, msgId)
    val chatlog = ChatMessage.getChatLog(server.b3.connection, 30, offset-15, None, false)
    Ok(views.html.chatlogWrapper(chatlog, 30, offset-15, request.user, msgId))
  }
}