package controllers

import models._
import play.api.Logger
import securesocial.core.RuntimeEnvironment
import play.api.cache._
import play.api.Play.current

class PenaltyController(override implicit val env: RuntimeEnvironment[UtAdminUser])
  extends securesocial.core.SecureSocial[UtAdminUser] {

  val log = Logger(this getClass() getName())

  val server = UtServer

  def allBans() = SecuredAction { request =>
    MongoLogger.logAction(request.user, "All bans")
    Ok(views.html.bans(PenaltyController.getPenalties(
      userId = None,
      banOnly = false,
      adminOnly = false,
      activeOnly = false,
      noticeOnly = false), "All Bans", request.user))
  }

  def adminBans(noticeOnly: Boolean) = SecuredAction { request =>
    MongoLogger.logAction(request.user, "Admin Bans")
    noticeOnly match {
      case false =>
        Ok(views.html.bans(PenaltyController.getPenalties(
          userId = None,
          banOnly = true,
          adminOnly = true,
          activeOnly = false,
          noticeOnly = false), "Admin Bans", request.user))
      case _ => Ok(views.html.bans(PenaltyController.getPenalties(
        userId = None,
        banOnly = false,
        adminOnly = true,
        activeOnly = false,
        noticeOnly = true), "Admin Notices", request.user))
    }
  }

  def punishPlayer(userId: Int, reason: String, penalty: String, duration: Option[String]) = SecuredAction {
    implicit request =>
      MongoLogger.logAction(request.user, "PUNISHING:" + userId + " " + reason)
      duration match {
        case None => PenaltyController.addPenalty(
          userId,
          PenaltyHandler.withName(penalty),
          request.user.main.userId + ": " + reason,
          None,
          request.user.b3Id
        )
        case Some(d) => PenaltyController.addPenalty(
          userId,
          PenaltyHandler.withName(penalty),
          reason,
          Some(PenaltyDuration.withName(duration.get)),
          request.user.b3Id)
      }
      Ok("Added")
  }

  def removePunishment(penaltyId: Int) = SecuredAction {
    implicit request =>
      MongoLogger.logAction(request.user, "Removing Punishment!")
      PenaltyController.deletePenalty(penaltyId)
      Ok("Removed")
  }

  def permBans() = SecuredAction {
    implicit request =>

      jassh.SSH.once(UtServer.sshIp, UtServer.sshUser, UtServer.sshPass) {
        ssh =>
          val banlisttxt = ssh.execute("cat " + UtServer.banlistTxtFile).split("\n")
          PenaltyController.updatePermBanTable(banlisttxt)
          Ok(views.html.permBans(banlisttxt,PenaltyController.getUsersDirectlyAffectedByPermBan,request.user))
      }
  }

}
