package controllers

import models._
import org.joda.time.{Duration, DateTime}
import play.api.Logger
import play.api.libs.json.Json
import scalikejdbc._
import securesocial.core.RuntimeEnvironment
import play.api.cache._
import play.api.Play.current
import models.Penalty.Formatters._

class PenaltyController(override implicit val env: RuntimeEnvironment[UtAdminUser])
  extends securesocial.core.SecureSocial[UtAdminUser] {

  val log = Logger(this getClass() getName())

  def removePunishment(penaltyId: Int) = SecuredAction {
    implicit request =>
      MongoLogger.logAction(request.user, "Removing Punishment!")
      PenaltyController.deletePenalty(penaltyId)
      Ok("Removed")
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


  def penaltiesTemplatHtml() = SecuredAction {
    implicit request =>
      Ok(views.html.penalties.render())
  }

  def getPenaltiesJson(count: Int,
                       page: Int,
                       userId: Option[Int],
                       queryString: Option[String],
                       filterType: Option[String],
                       activeOnly: Option[Boolean]) = SecuredAction {
    implicit request =>
      Ok(Json.toJson(
        PenaltyController.getPenalties(count,
          page,
          userId,
          queryString,
          filterType,
          activeOnly)))
  }
}

object PenaltyController {

  /** TODO: Change */
  def addPenalty(userId: Int,
                 penalty: PenaltyHandler.Value,
                 reason: String,
                 duration: Option[PenaltyDuration.Value],
                 adminId: Int) = DB localTx { implicit session =>
    val secondsOfOneDay = 86400L
    val (exp: Long, d: Long) = duration match {
      case None => (0L, 0L)
      case Some(PenaltyDuration.One_day) =>
        (DateTime.now().plusDays(1).getMillis / 1000L, secondsOfOneDay)
      case Some(PenaltyDuration.Two_days) =>
        (DateTime.now().plusDays(2).getMillis / 1000L, secondsOfOneDay * 2L)
      case Some(PenaltyDuration.One_week) =>
        (DateTime.now().plusWeeks(1).getMillis / 1000L, secondsOfOneDay * 7L)
      case Some(PenaltyDuration.Two_weeks) =>
        (DateTime.now().plusWeeks(2).getMillis / 1000L, secondsOfOneDay * 14L)
      case Some(PenaltyDuration.One_month) =>
        (DateTime.now().plusMonths(1).getMillis / 1000L, secondsOfOneDay * 30L)
      case _ => throw new IllegalArgumentException()
    }

    val expires = if (penalty == PenaltyHandler.Ban) sqls"-1" else sqls"$exp"

    sql"""INSERT INTO penalties
          VALUES(NULL, ${penalty.toString}, $userId, $adminId, $d, 0,
       'echelon', $reason,'', ${DateTime.now().getMillis / 1000},  ${DateTime.now().getMillis / 1000},$expires)""".execute().apply()
  }


  def deletePenalty(penaltyId: Int) = DB autoCommit { implicit session =>
    sql"DELETE FROM penalties WHERE penalties.id = $penaltyId".execute().apply()
  }


  def getPenaltyCountFor(userId: Int): Int = DB autoCommit { implicit session =>
    sql"SELECT count(*) FROM penalties WHERE penalties.client_id = ${userId}".map(rs => rs.int(1)).single().apply().getOrElse(0)
  }

  def getPenalties(count: Int,
                   page: Int,
                   userId: Option[Int],
                   queryString: Option[String],
                   filterType: Option[String],
                   activeOnly: Option[Boolean]) =
    DB readOnly { implicit session =>
      val offset = count * page


      val userIdCheck = if (userId.isDefined) sqls.and.eq(sqls"penalties.client_id", userId) else sqls.empty

      val ft = filterType.getOrElse("foo")

      val typeCheck = if (ft == "Warning" || ft == "Kick" || ft == "Ban" || ft == "Notice" || ft == "TempBan")
        sqls.and.like(sqls"penalties.type", ft)
      else sqls.empty

      val activeOnlyCheck = if (activeOnly.isDefined && activeOnly.get) sqls" AND penalties.inactive = 0 " else sqls.empty
      val queryStringCheck = if (queryString.isDefined) sqls.and.like(sqls"penalties.reason", s"%${queryString.get}%") else sqls.empty

      sql"""SELECT penalties.*, clients.name AS admin_name, c2.name AS client_name
          FROM ( penalties
          INNER JOIN clients ON clients.id = penalties.admin_id $userIdCheck $typeCheck $activeOnlyCheck $queryStringCheck )
          INNER JOIN clients AS c2 ON c2.id = penalties.client_id
          ORDER BY penalties.time_add DESC LIMIT $count OFFSET $offset """
        .map(rs => Penalty(
          penalty = rs.string("type"),
          penaltyId = rs.int("id"),
          adminId = rs.int("admin_id"),
          playerId = rs.int("client_id"),
          playerName = rs.string("client_name"),
          adminName = rs.string("admin_name"),
          reason = rs.string("reason"),
          created = new DateTime(rs.long("time_add") * 1000L),
          expires = {
            val dbExpires = rs.long("time_expire")
            if (dbExpires == -1 || dbExpires == 0) {
              None
            } else {
              Some(new DateTime(dbExpires * 1000L))
            }
          },
          inactive = rs.underlying.getInt("inactive") match {
            case 1 => true
            case _ => false
          },
          duration = rs.long("duration"),
          keyword = rs.string("keyword"),
          lastEdit = new DateTime(rs.long("time_edit") * 1000L)
        )).list().apply()
    }
}
