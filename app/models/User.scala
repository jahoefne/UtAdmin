package models

import java.sql.{ResultSet, Connection}

import org.joda.time.format.DateTimeFormat
import org.joda.time.{Hours, Period, DateTime}
import play.api.Logger

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
                group:Group,
                maskedAs: Option[Group],
                totalTimeOnServer: Period,
                xlrId: Option[Int])

object User {

  object UserSearch {

    val log = Logger(this getClass() getName())

    private def fromResultSet(set: ResultSet): Username = {
      Username(
        name = set.getString("alias"),
        firstUsed = new DateTime(set.getLong("time_edit") * 1000L),
        lastUsed = new DateTime(set.getLong("time_edit") * 1000L),
        userId = set.getInt("client_id"),
        usedCount = set.getInt("num_used")
      )
    }

    def findUser(conn: Connection, name: String): Seq[Username] = {
      log.debug(s"Finding user ${name}")
      val query = conn.prepareStatement( """
        SELECT * FROM `aliases` WHERE `aliases`.`alias` LIKE ?
        ORDER BY  `aliases`.`time_edit` DESC LIMIT 0 , 200 """)
      query.setString(1, '%' + name + '%')
      var users = Seq.empty[Username]
      val resultSet = query.executeQuery()
      while (resultSet.next())
        users :+= User.UserSearch.fromResultSet(resultSet)

      val query2 = conn.prepareStatement( """
        SELECT * FROM `clients` WHERE `clients`.`name` LIKE ?
        ORDER BY  `clients`.`time_edit` DESC LIMIT 0 , 200 """)
      query2.setString(1, '%' + name + '%')
      val resultSet2 = query2.executeQuery()
      while (resultSet2.next()) {
        val found = Username(
          name = resultSet2.getString("name"),
          firstUsed = new DateTime(resultSet2.getLong("time_add") * 1000L),
          lastUsed = new DateTime(resultSet2.getLong("time_edit") * 1000L),
          userId = resultSet2.getInt("id"),
          usedCount = 1)
        if (users.count(x => {
          x.userId == found.userId
        }) == 0)
          users :+= found
      }
      resultSet.close()
      resultSet2.close()
      query.close()
      query2.close()
      users
    }
  }

  object UserInfo {
    val log = Logger(this getClass() getName())

    def getOnlineHistory(conn: Connection, guid: String): List[(String, Long)] = {
      case class HistoryPlain(came: Long, gone: Long)
      val query = conn.prepareStatement( """
        SELECT * FROM `ctime` WHERE `ctime`.`guid` LIKE ?
        ORDER BY  `ctime`.`came` DESC LIMIT 0 , 70 """)
      query.setString(1, guid)
      var history = Seq.empty[HistoryPlain]
      val rs = query.executeQuery()

      while (rs.next())
        history :+= HistoryPlain(rs.getLong("came") * 1000L, rs.getLong("gone") * 1000L)

      val format = DateTimeFormat.forPattern("E dd.MM.yy")
      history.groupBy((x) => new DateTime(x.came).toString(format))
        .mapValues((y) => y.map((z) => (z.gone - z.came) / 1000 / 60).sum)
        .toList.sortWith((x, y) => {
        format.parseDateTime(x._1).isBefore(format.parseDateTime(y._1))
      }
        )
    }

    def getIpAliases(dbConn: Connection, b3Id: Int): Seq[IpAlias] = {
      val query = dbConn.prepareStatement("SELECT * FROM `ipaliases` WHERE `client_id` = ?")
      query.setInt(1, b3Id)
      val response = query.executeQuery()
      var result = Seq.empty[IpAlias]

      while (response.next()) {
        result :+= IpAlias(
          ip = response.getString("ip"),
          usedCount = response.getInt("num_used"),
          firstUsed = new DateTime(response.getLong("time_add") * 1000L),
          lastUsed = new DateTime(response.getLong("time_edit") * 1000L)
        )
      }
      result
    }

    def getTotalTimeOnServer(conn: Connection, guid: String): Period = {
      val q = conn.prepareStatement( """
        SELECT SUM( `ctime`.`gone`- `ctime`.`came` ) AS  `total_time`
        FROM  `ctime`
        WHERE  `ctime`.`guid` =  ? """)
      q.setString(1, guid)
      val rs = q.executeQuery()
      rs.next()
      new Period(new DateTime(0), new DateTime(rs.getLong("total_time") * 1000L))
    }

    def getAliases(dbConn: Connection, b3Id: Int): Seq[Username] = {
      val query = dbConn.prepareStatement("SELECT * FROM `aliases` WHERE `client_id` = ?")
      query.setInt(1, b3Id)
      val response = query.executeQuery()
      var result = Seq.empty[Username]

      while (response.next()) {
        result :+= Username(
          name = response.getString("alias"),
          firstUsed = new DateTime(response.getLong("time_add") * 1000L),
          lastUsed = new DateTime(response.getLong("time_edit") * 1000L),
          userId = response.getInt("client_id"),
          usedCount = response.getInt("num_used")
        )
      }
      result
    }

    def getXlrStatsId(dbConn: Connection, b3Id: Int): Option[Int] = {
      val query = dbConn.prepareStatement("SELECT * FROM `xlr_playerstats` WHERE `xlr_playerstats`.`client_id` = ? ")
      query.setInt(1, b3Id)
      val rs = query.executeQuery()
      rs.next() match {
        case true => Some(rs.getInt("id"))
        case _ => None
      }
    }

    def getUserByB3Id(dbConn: Connection, b3Id: Int): Option[User] = {
      val query = dbConn.prepareStatement("SELECT * FROM `clients` WHERE `clients`.`id` = ? ")
      query.setInt(1, b3Id)
      val resultSet = query.executeQuery()

      resultSet.next() match {
        case true =>
          val maskLevel = resultSet.getInt("mask_level")
          val guid = resultSet.getString("guid")

          val group = B3GroupController.getGroupForGroupBits(dbConn, resultSet.getInt("group_bits"))
          val maskedAs = B3GroupController.getGroupForGroupLevel(dbConn, resultSet.getInt("mask_level"))match {
            case x if x == group => None
            case x => Some(x)
          }

          Some(User(
            currentName = resultSet.getString("name"),
            aliases = this.getAliases(dbConn = dbConn, b3Id = b3Id),
            currentIp = resultSet.getString("ip"),
            ipAliases = this.getIpAliases(dbConn, b3Id),
            penalties = PenaltyController.getPenalties(
              dbConn = dbConn,
              userId = Some(b3Id),
              banOnly = false,
              adminOnly = false,
              activeOnly = false,
              noticeOnly = false),
            guid = guid,
            firstSeen = new DateTime(resultSet.getLong("time_add") * 1000L),
            lastSeen = new DateTime(resultSet.getLong("time_edit") * 1000L),
            numberOfConnections = resultSet.getInt("connections"),
            b3Id = b3Id,
            group = group,
            maskedAs = maskedAs,
            totalTimeOnServer = getTotalTimeOnServer(dbConn, guid),
            xlrId = getXlrStatsId(dbConn, b3Id)
          )
          )
        case _ =>
          None
      }
    }
  }

}
