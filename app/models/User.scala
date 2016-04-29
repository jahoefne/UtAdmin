package models

import java.sql.{ResultSet, Connection}

import org.joda.time.format.DateTimeFormat
import org.joda.time.{Hours, Period, DateTime}
import play.api.Logger
import scalikejdbc._

import scala.tools.nsc.doc.model.Visibility


case class Username(name: String,
                    firstUsed: DateTime,
                    lastUsed: DateTime,
                    userId: Int,
                    usedCount: Int)

case class IpAlias(ip: String,
                   usedCount: Int,
                   firstUsed: DateTime,
                   lastUsed: DateTime)

case class User(currentName: String,
                aliases: Seq[Username],
                currentIp: String,
                ipAliases: Seq[IpAlias],
                penalties: Seq[Penalty],
                guid: String,
                firstSeen: DateTime,
                lastSeen: DateTime,
                numberOfConnections: Int,
                b3Id: Int,
                group: Group,
                maskedAs: Option[Group],
                totalTimeOnServer: Period,
                xlrId: Option[Int],
                xlrVisible: Boolean)

object User {

  object UserSearch {

    val log = Logger(this getClass() getName())

    private def fromResultSet(set: WrappedResultSet): Username = {
      Username(
        name = set.string("alias"),
        firstUsed = new DateTime(set.long("time_edit") * 1000L),
        lastUsed = new DateTime(set.long("time_edit") * 1000L),
        userId = set.int("client_id"),
        usedCount = set.int("num_used")
      )
    }

    def findUser(query: String): Seq[Username] = DB readOnly { implicit session =>
      val likeAlias = sqls.like(sqls"`alias`", "%" + query + "%")
      val likeName = sqls.like(sqls"`name`", "%" + query + "%")
      val likeIp = sqls.like(sqls"`ip`", "%" + query + "%")
      val likeGuid = sqls.like(sqls"`guid`", "%" + query + "%")
      val likeIpAlias = sqls.like(sqls"`ipaliases`.`ip`", "%" + query + "%")

      sql"""SELECT * FROM aliases WHERE $likeAlias ORDER BY time_edit DESC LIMIT 0, 500"""
        .map(set => Username(
          name = set.string("alias"),
          firstUsed = new DateTime(set.long("time_edit") * 1000L),
          lastUsed = new DateTime(set.long("time_edit") * 1000L),
          userId = set.int("client_id"),
          usedCount = set.int("num_used")
        )).list().apply().toSeq.union(
        sql"""SELECT * FROM clients WHERE $likeName OR $likeIp OR $likeGuid ORDER BY time_edit DESC LIMIT 0, 500"""
          .map(rs => Username(
            name = rs.string("name"),
            firstUsed = new DateTime(rs.long("time_add") * 1000L),
            lastUsed = new DateTime(rs.long("time_edit") * 1000L),
            userId = rs.int("id"),
            usedCount = 1)
          ).list().apply().toSeq).union(
        sql"""SELECT DISTINCT(clients.id), clients.name, clients.time_edit, clients.time_add FROM clients
              INNER JOIN ipaliases ON clients.id = ipaliases.id WHERE $likeIpAlias"""
          .map(rs => Username(
            name = rs.string("name"),
            firstUsed = new DateTime(rs.long("time_add") * 1000L),
            lastUsed = new DateTime(rs.long("time_edit") * 1000L),
            userId = rs.int("id"),
            usedCount = 1)).list().apply().toSeq
      )
    }
  }

  object UserInfo {
    val log = Logger(this getClass() getName())

    def getOnlineHistory(guid: String): List[(DateTime, DateTime)] = DB readOnly { implicit session =>
      sql"SELECT  * FROM ctime WHERE guid LIKE $guid ORDER BY came DESC LIMIT 0, 70"
        .map(rs => (new DateTime(rs.long("came") * 1000L), new DateTime(rs.long("gone") * 1000L))).list.apply()
    }

    def getOnlineHistoryChartData(guid: String): List[(String, Long)] = DB readOnly { implicit session =>
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

    def getIpAliases(b3Id: Int): Seq[IpAlias] = DB readOnly { implicit session =>
      sql"SELECT  * FROM ipaliases WHERE  client_id = $b3Id"
        .map(rs => IpAlias(ip = rs.string("ip"),
          usedCount = rs.int("num_used"),
          firstUsed = new DateTime(rs.long("time_add") * 1000L),
          lastUsed = new DateTime(rs.long("time_edit") * 1000L))).list().apply().toSeq
    }

    def getTotalTimeOnServer(guid: String): Period = DB readOnly { implicit session =>
      sql"SELECT SUM(ctime.gone - ctime.came) AS total_time FROM ctime WHERE guid = $guid"
        .map(rs => new Period(new DateTime(0), new DateTime(rs.longOpt("total_time").getOrElse(0L) * 1000L)))
        .first().apply().getOrElse(new Period())
    }

    def getAliases(b3Id: Int): Seq[Username] = DB readOnly { implicit session =>
      sql"SELECT * FROM aliases WHERE client_id = $b3Id"
        .map(rs => Username(name = rs.string("alias"),
          firstUsed = new DateTime(rs.long("time_add") * 1000L),
          lastUsed = new DateTime(rs.long("time_edit") * 1000L),
          userId = rs.int("client_id"),
          usedCount = rs.int("num_used"))).list().apply()
    }

    def getXlrStatsId(b3Id: Int): Option[Int] = DB readOnly { implicit session =>
      sql"SELECT id FROM xlr_playerstats WHERE xlr_playerstats.client_id = $b3Id".map(rs => rs.int("id")).first().apply()
    }

    def getXlrStatsVisibility(b3Id:Int) : Boolean = DB readOnly{implicit session =>
      sql"SELECT * FROM xlr_playerstats WHERE client_id = $b3Id".map(_.boolean("hide")).first().apply().getOrElse(false)
    }

    def setXlrStatsVisibility(b3Id:Int, visibility: Boolean ) : Boolean = DB localTx { implicit session =>
      sql"UPDATE xlr_playerstats SET hide = ${!visibility} WHERE client_id = $b3Id".execute().apply()
    }

    def getUserByB3Id(b3Id: Int): Option[User] = DB readOnly { implicit session =>
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
              aliases = this.getAliases(b3Id = b3Id),
              currentIp = rs.string("ip"),
              ipAliases = this.getIpAliases(b3Id),
              penalties = PenaltyController.getPenalties(
                userId = Some(b3Id),
                banOnly = false,
                adminOnly = false,
                activeOnly = false,
                noticeOnly = false),
              guid = guid,
              firstSeen = new DateTime(rs.long("time_add") * 1000L),
              lastSeen = new DateTime(rs.long("time_edit") * 1000L),
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
