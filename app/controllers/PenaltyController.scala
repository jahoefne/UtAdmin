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


  def getPenaltiesJson(count: Int,
                       page: Int,
                       userId: Option[Int],
                       queryString: Option[String],
                       banOnly: Option[Boolean],
                       adminOnly: Option[Boolean],
                       activeOnly: Option[Boolean],
                       noticeOnly: Option[Boolean]) = SecuredAction {
    implicit request =>
      Ok(Json.toJson(PenaltyController.getPenalties(count, page, userId,  queryString, banOnly,  adminOnly, activeOnly, noticeOnly)))
  }
}

object PenaltyController {
  def getPenalties(count: Int,
                   page: Int,
                   userId: Option[Int],
                   queryString: Option[String],
                   banOnly: Option[Boolean],
                   adminOnly: Option[Boolean],
                   activeOnly: Option[Boolean],
                   noticeOnly: Option[Boolean]) =
    DB readOnly { implicit session =>
      val offset = count * page


      val userIdCheck = if (userId.isDefined) sqls" AND penalties.client_id = $userId " else sqls""
      val adminOnlyCheck = if (adminOnly.isDefined && adminOnly.get) sqls" AND penalties.admin_id != 0 " else sqls""
      val banOnlyCheck = if (banOnly.isDefined && banOnly.get) sqls" AND ( penalties.type LIKE 'Ban' OR penalties.type LIKE 'TempBan') " else sqls""
      val noticeOnlyCheck = if (noticeOnly.isDefined && noticeOnly.get) sqls" AND (penalties.type LIKE 'Notice' ) " else sqls""
      val activeOnlyCheck = if (activeOnly.isDefined && activeOnly.get) sqls" AND penalties.inactive = 0 " else sqls""
      val queryStringCheck = if (queryString.isDefined) sqls.and.like(sqls"penalties.reason", s"%${queryString.get}") else sqls""


      println(queryStringCheck.toString())
      sql"""SELECT penalties.*, clients.name AS admin_name, c2.name AS client_name
          FROM ( penalties
          INNER JOIN clients ON clients.id = penalties.admin_id $userIdCheck $adminOnlyCheck $banOnlyCheck $noticeOnlyCheck $activeOnlyCheck $queryStringCheck )
          INNER JOIN clients AS c2 ON c2.id = penalties.client_id
          ORDER BY penalties.id DESC LIMIT $count OFFSET $offset """
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
        .filter(x => if (activeOnly.isDefined && activeOnly.get && x.expires.isDefined && x.expires.get.isBefore(DateTime.now())) false else true)
        .toSeq
        .sortWith((x: Penalty, y: Penalty) => {
          x.expires.isEmpty && y.expires.isDefined ||
            x.expires.isDefined && y.expires.isDefined && x.expires.get.isAfter(y.expires.get)
        })
    }
}
