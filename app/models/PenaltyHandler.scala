package models

import java.sql.Connection
import org.joda.time.{Duration, DateTime}
import scala.util.control.Breaks._

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

  def deletePenalty(dbConn: Connection, penaltyId: Int) = {
    val q = dbConn.prepareStatement( """DELETE FROM `penalties` WHERE `penalties`.`id` = ?""")
    q.setInt(1, penaltyId)
    q.execute()
  }

  def addPenalty(dbConn: Connection,
                 userId: Int,
                 penalty: PenaltyHandler.Value,
                 reason: String,
                 duration: Option[PenaltyDuration.Value],
                adminId: Int) = {
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

    val ins = dbConn.prepareStatement(
      """INSERT INTO `penalties`
       VALUES ( NULL, ?, ?, ?, ?, ?, ?, ?, ' ', ?, ?, ?)
      """)
    ins.setString(1, penalty.toString)
    ins.setInt(2, userId) // client id
    ins.setInt(3, adminId) // admin id
    ins.setLong(4, d) // duration
    ins.setInt(5, 0) // inactive
    ins.setString(6, "echelon") // keyword
    ins.setString(7, reason) // reason
    ins.setLong(8, DateTime.now().getMillis / 1000) // time_add
    ins.setLong(9, DateTime.now().getMillis / 1000) // time_edit
    ins.setLong(10, if (penalty == PenaltyHandler.Ban) {
      -1
    } else exp) // time_expire
    println(ins.toString)
    ins.execute()
  }

  def getPenalties(dbConn: Connection,
                   userId: Option[Int],
                   banOnly: Boolean,
                   adminOnly: Boolean,
                   activeOnly: Boolean,
                   noticeOnly: Boolean): Seq[Penalty] = {

    val query = dbConn.prepareStatement(
      """SELECT `penalties`.*, `clients`.`name` as `admin_name`, `c2`.`name` as `client_name`
        FROM ( `penalties`
        INNER JOIN `clients`
        ON `clients`.`id` = `penalties`.`admin_id` """ + {
        if (userId.isDefined)
          "AND `penalties`.`client_id` = ? "
        else
          " "
      } + {
        if (adminOnly) {
          " AND `penalties`.`admin_id` != 0 "
        } else {
          " "
        }
      } + {
        if (banOnly)
          " AND ( `penalties`.`type` LIKE 'Ban' OR `penalties`.`type` LIKE 'TempBan' ) "
        else
          " "
      } + {
        if (noticeOnly)
          " AND ( `penalties`.`type` LIKE 'Notice' ) "
        else
          " "
      } + {
        if (activeOnly)
          " AND `penalties`.`inactive` = 0 "
        else
          " "
      }
        + " ) INNER JOIN `clients` as `c2` ON `c2`.`id` = `penalties`.`client_id` "
    )

    if (userId.isDefined) {
      query.setInt(1, userId.get)
    }

    val response = query.executeQuery()
    var result = Seq.empty[Penalty]

    while (response.next()) {
      breakable {
        val dbExpires = response.getLong("time_expire")
        val expires = {
          if (dbExpires == -1 || dbExpires == 0) {
            None
          } else {
            Some(new DateTime(dbExpires * 1000L))
          }
        }
        if (activeOnly && expires.isDefined) {
          if (expires.get.isBefore(DateTime.now()))
            break()
        }

        result :+= Penalty(
          penalty = PenaltyHandler.withName(response.getString("type")),
          penaltyId = response.getInt("id"),
          adminId = response.getInt("admin_id"),
          playerId = response.getInt("client_id"),
          playerName = response.getString("client_name"),
          adminName = response.getString("admin_name"),
          reason = response.getString("reason"),
          created = new DateTime(response.getLong("time_add") * 1000L),
          expires = expires,
          inactive = response.getInt("inactive") match {
            case 1 => true
            case _ => false
          },
          duration = new Duration(response.getLong("duration") * 1000L),
          keyword = response.getString("keyword"),
          lastEdit = new DateTime(response.getLong("time_edit") * 1000L)
        )
      }
    }

    query.close()
    response.close()
    result.sortWith((x: Penalty, y: Penalty) => {
      x.expires.isEmpty && y.expires.isDefined ||
        x.expires.isDefined && y.expires.isDefined && x.expires.get.isAfter(y.expires.get)
    })
  }


}
