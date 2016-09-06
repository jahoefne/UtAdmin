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

object Penalty{
  object Formatters {
    implicit val penaltyFormat = Json.format[Penalty]
  }
}