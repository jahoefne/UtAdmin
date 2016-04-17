/**
  * This code is pretty dirty and needs to be cleaned up
  */
package models

import java.sql.{ResultSet, Connection}

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.Logger
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import scala.concurrent.duration._

case class B3User(name: String,
                  ip: String,
                  id: Int,
                  connections: Int,
                  guid: String,
                  pbid: String,
                  group: Group,
                  firstSeen: DateTime,
                  lastSeen: DateTime)

case class OnlineUser(name: String,
                      ip: String,
                      id: Int,
                      connections: Int,
                      group: Group,
                      team: Int,
                      score: Int,
                      joined: DateTime,
                      serverId: Int,
                      xlrId: Int)


object B3UserController {
  val log = Logger(this getClass() getName())
  private def b3UserFromResultSet(set: ResultSet): B3User = {
    B3User(
      name = set.getString("name"),
      ip = set.getString("ip"),
      id = set.getInt("id"),
      connections = set.getInt("connections"),
      guid = set.getString("guid"),
      pbid = set.getString("pbid"),
      group = Group(bits = set.getInt("group_bits"), level = set.getInt("group_level"), name = set.getString("group_name")),
      firstSeen = new DateTime(set.getLong("time_add") * 1000L),
      lastSeen = new DateTime(set.getLong("time_edit") * 1000L)
    )
  }

  def getUsers(conn: Connection, count: Int, offset: Int, minLevel: Int): Seq[B3User] = {
    val query = conn.prepareStatement(
      """SELECT `clients`.*, `groups`.`name` as `group_name`, `groups`.`id` as `group_bits`, `groups`.`level` as `group_level`
       FROM ( `clients` INNER JOIN `groups` ON `groups`.`id` = `clients`.`group_bits` )
       WHERE `level` >= ? ORDER BY  `clients`.`time_edit` DESC LIMIT ?, ? """)

    query.setInt(1, minLevel)
    query.setInt(2, offset)
    query.setInt(3, count)
    var users = Seq.empty[B3User]
    val resultSet = query.executeQuery()
    while (resultSet.next())
      users :+= B3UserController.b3UserFromResultSet(resultSet)
    query.close()
    resultSet.close()
    users
  }

  def listOnlineUsers(conn: Connection): Seq[OnlineUser] = {
   // log.info("Querying online users!")
    val query = conn.prepareStatement(
      """SELECT
           `current_clients`.* ,
           `clients`.`time_edit` ,
           `xlr_playerstats`.`id` AS  `xlr_id`,
           `groups`.`name` AS `group_name`,
           `groups`.`id` AS `group_bits`,
           `groups`.`level` AS `group_level`
         FROM
           `current_clients`,
           `clients`,
           `xlr_playerstats`,
           `groups`
         WHERE  `current_clients`.`guid` = `clients`.`GUID`
                 AND  `xlr_playerstats`.`client_id` =  `current_clients`.`DBID`
                 AND `current_clients`.`level` = `groups`.`level`""")

    var users = Seq.empty[OnlineUser]
    val set = query.executeQuery()
    while (set.next())
      users :+= OnlineUser(
        name = set.getString("ColorName"),
        ip = set.getString("ip"),
        id = set.getInt("DBID"),
        connections = set.getInt("Connections"),
        group = Group(
          bits = set.getInt("group_bits"),
          level = set.getInt("group_level"),
          name = set.getString("group_name")),
        score = set.getInt("Score"),
        team = set.getInt("Team"),
        joined = new DateTime(set.getLong("time_edit") * 1000L),
        serverId = set.getInt("CID"),
        xlrId = set.getInt("xlr_id"))
    query.close()
    set.close()
    users
  }
}
