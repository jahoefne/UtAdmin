package models

import java.sql.Connection
import org.joda.time.{Duration, DateTime}
import play.api.libs.json.Json
import scala.util.control.Breaks._
import scalikejdbc._

case class Penalty(penalty: String,
                   penaltyId: Int,
                   playerId: Int,
                   playerName: String,
                   adminId: Int,
                   adminName: String,
                   duration: Long,
                   inactive: Boolean,
                   reason: String,
                   keyword: String,
                   created: DateTime,
                   expires: Option[DateTime],
                   lastEdit: DateTime)



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

object Penalty{
  object Formatters {
    implicit val penaltyFormat = Json.format[Penalty]
  }
}