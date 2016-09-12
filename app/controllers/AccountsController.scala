package controllers

import models._
import play.api.libs.json.Json
import securesocial.core.providers.utils.PasswordHasher
import securesocial.core.{RuntimeEnvironment, AuthenticationMethod, BasicProfile}
import scala.concurrent.duration._
import UtAdminUserService.Formatters._
import scala.concurrent.Await

/**
  * Created by jahoefne on 12/09/16.
  */
class AccountsController(override implicit val env: RuntimeEnvironment[UtAdminUser]) extends securesocial.core.SecureSocial[UtAdminUser] {

  def accountsJson() = SecuredAction { request =>
    MongoLogger.logAction(request.user, "Admin Accounts")
    if (request.user.rank == Ranks.God) {
      Ok(Json.toJson(UtAdminUserService.getAllUsers))
    } else {
      Unauthorized("You have no power here")
    }
  }


  def accountsTemplateHtml() = SecuredAction { request =>
    Ok(views.html.accounts())
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
        Ok("Done")
      } else {
        MongoLogger.logAction(request.user, "COULD NOT DELETE USER, RANK NOT HIGH ENOUGH " + user)
        Unauthorized("You have no power here")
      }
  }

  /** Add a new UtAdmin user or update an exsiting one */
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
}
