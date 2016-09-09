package models

import java.sql.{ResultSet, Connection}

import controllers.PenaltyController
import org.joda.time.format.DateTimeFormat
import org.joda.time.{Hours, Period, DateTime}
import play.api.Logger
import play.api.libs.json.Json
import scalikejdbc._

import scala.Int.MaxValue
import scala.tools.nsc.doc.model.Visibility


case class Username(name: String,
                    firstUsed: Long,
                    lastUsed: Long,
                    userId: Int,
                    usedCount: Int)

case class IpAlias(ip: String,
                   usedCount: Int,
                   firstUsed: Long,
                   lastUsed: Long)

case class User(currentName: String,
                aliases: Seq[Username],
                currentIp: String,
                ipAliases: Seq[IpAlias],
                penaltyCount: Int,
                guid: String,
                firstSeen: Long,
                lastSeen: Long,
                numberOfConnections: Int,
                b3Id: Int,
                group: Group,
                maskedAs: Option[Group],
                totalTimeOnServer: Long,
                xlrId: Option[Int],
                xlrVisible: Boolean)

object User {


  object Formatters {
    implicit val uName = Json.format[Username]
    implicit val ipAlias = Json.format[IpAlias]
    implicit val group = Json.format[Group]
    implicit val user = Json.format[User]
  }

  object UserInfo {

    val log = Logger(this getClass() getName())


    def setXlrStatsVisibility(b3Id: Int, visibility: Boolean): Boolean = DB autoCommit  { implicit session =>
      sql"UPDATE xlr_playerstats SET hide = ${!visibility} WHERE client_id = $b3Id".execute().apply()
    }


    def getUserByB3Id(b3Id: Int): Option[User] = DB readOnly { implicit session =>

      def getOnlineHistoryChartData(guid: String): List[(String, Long)] = {
        case class HistoryPlain(came: Long, gone: Long)
        val format = DateTimeFormat.forPattern("E dd.MM.yy")
        sql"SELECT * FROM ctime WHERE guid LIKE $guid ORDER BY came DESC LIMIT 0, 70"
          .map(rs => HistoryPlain(rs.long("came") * 1000L, rs.long("gone") * 1000L)).list().apply()
          .groupBy((x) => new DateTime(x.came).toString(format))
          .mapValues((y) => y.map((z) => (z.gone - z.came) / 1000 / 60).sum)
          .toList.sortWith((x, y) => {
          format.parseDateTime(x._1).isBefore(format.parseDateTime(y._1))
        })
      }

      def getIpAliases(b3Id: Int): Seq[IpAlias] = {
        sql"SELECT  * FROM ipaliases WHERE  client_id = $b3Id"
          .map(rs => IpAlias(ip = rs.string("ip"),
            usedCount = rs.int("num_used"),
            firstUsed = rs.long("time_add"),
            lastUsed = rs.long("time_edit"))).list().apply().toSeq
      }

      def getTotalTimeOnServer(guid: String): Long = {
        sql"SELECT SUM(ctime.gone - ctime.came) AS total_time FROM ctime WHERE guid = $guid"
          .map(rs => rs.longOpt(1)).single().apply().get.getOrElse(0)
      }

      def getAliases(b3Id: Int): Seq[Username] = {
        sql"SELECT * FROM aliases WHERE client_id = $b3Id"
          .map(rs => Username(name = rs.string("alias"),
            firstUsed = rs.long("time_add"),
            lastUsed = rs.long("time_edit"),
            userId = rs.int("client_id"),
            usedCount = rs.int("num_used"))).list().apply()
      }

      def getXlrStatsId(b3Id: Int): Option[Int] = {
        sql"SELECT id FROM xlr_playerstats WHERE xlr_playerstats.client_id = $b3Id".map(rs => rs.int("id")).first().apply()
      }

      def getXlrStatsVisibility(b3Id: Int): Boolean = {
        sql"SELECT * FROM xlr_playerstats WHERE client_id = $b3Id".map(_.boolean("hide")).first().apply().getOrElse(false)
      }

      sql"""SELECT * FROM clients WHERE id = $b3Id"""
        .map(rs => {
          val maskLevel = rs.underlying.getInt("mask_level")
          val guid = rs.string("guid")
          val group = B3GroupController.getGroupForGroupLevel(rs.int("group_bits"))
          val maskedAs = B3GroupController.getGroupForGroupLevel(rs.underlying.getInt("mask_level")) match {
            case x if x == group => None
            case x => Some(x)
          }
          User(
            currentName = rs.string("name"),
            aliases = getAliases(b3Id = b3Id),
            currentIp = rs.string("ip"),
            ipAliases = getIpAliases(b3Id),
            penaltyCount = PenaltyController.getPenaltyCountFor(b3Id),
            guid = guid,
            firstSeen = rs.long("time_add"),
            lastSeen = rs.long("time_edit"),
            numberOfConnections = rs.int("connections"),
            b3Id = b3Id,
            group = group,
            maskedAs = maskedAs,
            totalTimeOnServer = getTotalTimeOnServer(guid),
            xlrId = getXlrStatsId(b3Id),
            xlrVisible = getXlrStatsVisibility(b3Id)
          )
        }).first().apply()
    }
  }

}
