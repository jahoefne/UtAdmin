package controllers

import models._
import play.api.Logger
import securesocial.core.providers.utils.PasswordHasher
import securesocial.core.{AuthenticationMethod, BasicProfile, RuntimeEnvironment}
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.language.postfixOps

class Administrator(override implicit val env: RuntimeEnvironment[UtAdminUser]) extends securesocial.core.SecureSocial[UtAdminUser] {

  val log = Logger(this getClass() getName())

  val server = UtServer

  /** Restart the server by sending a command using ssh */
  def restartServer = SecuredAction {
    request =>
      MongoLogger.logAction(request.user, "Restarting Urt Server!")

      jassh.SSH.once(UtServer.sshIp, UtServer.sshUser, UtServer.sshPass) {
        ssh =>
          print(ssh.execute(UtServer.restartServerCmd))
      }
      Ok("Done.")
  }


  def accounts() = SecuredAction { request =>
    MongoLogger.logAction(request.user, "Admin Accounts")
    if (request.user.rank == Ranks.God) {
      Ok(views.html.accounts(UtAdminUserService.getAllUsers, request.user))
    } else {
      Unauthorized("You have no power here")
    }
  }

  def addUser(user: String, password: String, rank: Int, b3Id: Int) = SecuredAction {
    request =>
      MongoLogger.logAction(request.user, "Add User:" + user + " Rank: " + rank)
      if (request.user.rank == Ranks.God) {
        rank match {
          case Ranks.God | Ranks.Admin | Ranks.Mod =>
            Await.result(UtAdminUserService.find("userpass", user.toLowerCase), 2 second) match {
              /** Username doesn't exist yet, add the user */
              case None =>
                UtAdminUserService.insertUtAdminUser(new UtAdminUser(
                  rank = rank,
                  main = new BasicProfile(providerId = "userpass",
                    userId = user.toLowerCase,
                    firstName = Some("Admin"),
                    lastName = Some("Admin"),
                    fullName = Some("Admin"),
                    email = Some("Admin"),
                    avatarUrl = None,
                    authMethod = AuthenticationMethod.UserPassword,
                    oAuth1Info = None,
                    oAuth2Info = None,
                    passwordInfo = Some(new PasswordHasher.Default().hash(password))),
                  b3Id = b3Id))
                Ok("Added user!")

               /** user existed already, update the profile */
              case Some(u) =>
                UtAdminUserService.insertUtAdminUser(new UtAdminUser(
                  rank = rank,
                  main = new BasicProfile(providerId = "userpass",
                    userId = user.toLowerCase,
                    firstName = Some("Admin"),
                    lastName = Some("Admin"),
                    fullName = Some("Admin"),
                    email = Some("Admin"),
                    avatarUrl = None,
                    authMethod = AuthenticationMethod.UserPassword,
                    oAuth1Info = None,
                    oAuth2Info = None,
                    passwordInfo = u.passwordInfo),
                  b3Id = b3Id))
                Ok("Updated user!")
            }
          case _ => NotAcceptable(views.html.plainError("Rank invalid"))
        }
      } else {
        Unauthorized("You have no power here!")
      }

  }

  def changeGroupOfUser(userId: Int, groupBits: Int) = SecuredAction {
    request =>
      MongoLogger.logAction(request.user, "Changing group for user " + userId + " to bits: " + groupBits)
      request.user.rank match {
        case Ranks.God =>
          B3GroupController.setGroupForUser(UtServer.b3.connection, userId, groupBits)
          Redirect(request.request.headers.get("referer").getOrElse("/"))
        case _ =>
          Unauthorized("You have no power here")
      }
  }

  def changePassword(password: String) = SecuredAction {
    request =>
      UtAdminUserService.updatePasswordInfo(request.user, new PasswordHasher.Default().hash(password))
      Ok("Changed")
  }

  def deleteUser(user: String) = SecuredAction {
    request =>
      MongoLogger.logAction(request.user, "Trying to delete user:" + user)
      if (request.user.rank == Ranks.God) {
        MongoLogger.logAction(request.user, "Deleting user: " + user)
        UtAdminUserService.deleteUser(user)
        Ok(views.html.accounts(UtAdminUserService.getAllUsers, request.user))
      } else {
        MongoLogger.logAction(request.user, "COULD NOT DELETE USER, RANK NOT HIGH ENOUGH " + user)
        Unauthorized("You have no power here")
      }
  }

  def allLogs() = SecuredAction {
    request =>
      MongoLogger.logAction(request.user, "Accessing logs")
      if (request.user.rank == Ranks.God) {
        Ok(views.html.logs(MongoLogger.readLogs(), request.user))
      } else {
        BadRequest("")
      }
  }

  def passwordChange() = SecuredAction {
    request =>
      Ok(views.html.passwordChange(request.user))
  }

  def restartB3() = SecuredAction {
    request =>
      MongoLogger.logAction(request.user, "Restarting Urt B3!")

      jassh.SSH.once(UtServer.sshIp, UtServer.sshUser, UtServer.sshPass) {
        ssh =>
          print(ssh.execute(UtServer.restartB3Cmd))
      }
      Ok("Done.")
  }
}
