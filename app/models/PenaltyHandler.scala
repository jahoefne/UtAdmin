package models

import java.sql.Connection
import org.joda.time.{Duration, DateTime}
import scala.util.control.Breaks._
import scalikejdbc._

object PenaltyHandler extends Enumeration {
  val Ban = Value("Ban")
  val TempBan = Value("TempBan")
  val Kick = Value("Kick")
  val Warning = Value("Warning")
  val Notice = Value("Notice")
}

object PenaltyDuration extends Enumeration {
  val One_day = Value("1 day")
  val Two_days = Value("2 days")
  val One_week = Value("1 week")
  val Two_weeks = Value("2 weeks")
  val One_month = Value("1 month")
}

case class Penalty(penalty: PenaltyHandler.Value,
                   penaltyId: Int,
                   playerId: Int,
                   playerName: String,
                   adminId: Int,
                   adminName: String,
                   duration: Duration,
                   inactive: Boolean,
                   reason: String,
                   keyword: String,
                   created: DateTime,
                   expires: Option[DateTime],
                   lastEdit: DateTime)

object PenaltyController {

  def deletePenalty(penaltyId: Int) = DB localTx { implicit session =>
    sql"DELETE FROM penalties WHERE penalties.id = $penaltyId".execute().apply()
  }

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

  def getPenalties(userId: Option[Int], banOnly: Boolean, adminOnly: Boolean, activeOnly: Boolean, noticeOnly: Boolean): Seq[Penalty] =
    DB readOnly { implicit session =>
      val userIdCheck = if (userId.isDefined) sqls"AND penalties.client_id = $userId" else sqls""
      val adminOnlyCheck = if (adminOnly) sqls"AND penalties.admin_id != 0" else sqls""
      val banOnlyCheck = if (banOnly) sqls"AND ( penalties.type LIKE 'Ban' OR penalties.type LIKE 'TempBan')" else sqls""
      val noticeOnlyCheck = if (noticeOnly) sqls"AND (penalties.type LIKE 'Notice' )" else sqls""
      val activeOnlyCheck = if (noticeOnly) sqls"AND penalties.inactive = 0 " else sqls""

      sql"""SELECT penalties.*, clients.name as admin_name, c2.name as client_name
          FROM ( penalties
          INNER JOIN clients ON clients.id = penalties.admin_id
          $userIdCheck
          $adminOnlyCheck
          $banOnlyCheck
          $noticeOnlyCheck
          $activeOnlyCheck
          ) INNER JOIN clients as c2 ON c2.id = penalties.client_id"""
        .map(rs => Penalty(
          penalty = PenaltyHandler.withName(rs.string("type")),
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
          duration = new Duration(rs.long("duration") * 1000L),
          keyword = rs.string("keyword"),
          lastEdit = new DateTime(rs.long("time_edit") * 1000L)
        )).list().apply()
        .filter(x => if (activeOnly && x.expires.isDefined && x.expires.get.isBefore(DateTime.now())) false else true)
        .toSeq
        .sortWith((x: Penalty, y: Penalty) => {
          x.expires.isEmpty && y.expires.isDefined ||
            x.expires.isDefined && y.expires.isDefined && x.expires.get.isAfter(y.expires.get)
        })
    }
}