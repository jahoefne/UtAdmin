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

}
