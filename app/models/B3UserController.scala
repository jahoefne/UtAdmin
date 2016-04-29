/**
  * This code is pretty dirty and needs to be cleaned up
  */
package models

import java.sql.Connection

import org.joda.time.DateTime
import play.api.Logger
import scalikejdbc._

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

  private def b3UserFromResultSet(set: WrappedResultSet): B3User =  B3User(
      name = set.string("name"),
      ip = set.string("ip"),
      id = set.int("id"),
      connections = set.int("connections"),
      guid = set.string("guid"),
      pbid = set.string("pbid"),
      group = Group(bits = set.int("group_bits"),
        level = set.int("group_level"),
        name = set.string("group_name")),
      firstSeen = new DateTime(set.long("time_add") * 1000L),
      lastSeen = new DateTime(set.long("time_edit") * 1000L)
    )

  def getUsers(count: Int, offset: Int, minLevel: Int): Seq[B3User] = DB readOnly { implicit session =>
    sql"""SELECT
          clients.*,
          groups.name as group_name,
          groups.id as group_bits,
          groups.level as group_level
       FROM
       ( clients INNER JOIN groups ON groups.id = clients.group_bits)
       WHERE level >= $minLevel
       ORDER BY  clients.time_edit DESC LIMIT $offset, $count""".map(b3UserFromResultSet).list.apply().toSeq
  }

  def listOnlineUsers: Seq[OnlineUser] = DB readOnly { implicit session =>
    sql"""SELECT current_clients.*,
          clients.time_edit,
          xlr_playerstats.id AS  xlr_id,
          groups.name AS group_name,
          groups.id AS group_bits,
          groups.level AS group_level
      FROM current_clients, clients, xlr_playerstats, groups
      WHERE current_clients.guid = clients.guid
      AND xlr_playerstats.client_id = current_clients.dbid
      AND current_clients.level = groups.level"""
      .map(rs => OnlineUser(
        name = rs.string("ColorName"),
        ip = rs.string("ip"),
        id = rs.int("DBID"),
        connections = rs.int("Connections"),
        group = Group(
          bits = rs.int("group_bits"),
          level = rs.int("group_level"),
          name = rs.string("group_name")),
        score = rs.int("Score"),
        team = rs.underlying.getInt("Team"),
        joined = new DateTime(rs.long("time_edit") * 1000L),
        serverId = rs.int("CID"),
        xlrId = rs.int("xlr_id"))).list().apply().toSeq
  }
}
