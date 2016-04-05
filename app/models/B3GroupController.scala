package models

import java.sql.{ResultSet, Connection}

import controllers.UtServer
import org.joda.time.DateTime

case class Group(bits: Int, level: Int, name: String)

object B3GroupController {

  private def groupFromRs(rs: ResultSet) = {
    rs.next()
    Group(
      bits = rs.getInt("id"),
      level = rs.getInt("level"),
      name = rs.getString("name")
    )
  }

  def getGroups(): Seq[Group] = {
    val query = UtServer.b3.connection.prepareStatement("SELECT * FROM `groups`")
    val rs = query.executeQuery()
    var result = Seq.empty[Group]
    while (rs.next()) {
      result :+= Group(
        bits = rs.getInt("id"),
        level = rs.getInt("level"),
        name = rs.getString("name")
      )
    }
    query.close()
    rs.close()
    result.sortWith(_.level > _.level)
  }

  def getGroupForGroupLevel(dbConn: Connection, i: Int): Group = {
    val q = dbConn.prepareStatement("SELECT * FROM `groups` WHERE `groups`.`id` = ?")
    q.setInt(1, i)
    val x = groupFromRs(q.executeQuery())
    q.close()
    x
  }

  def getGroupForGroupBits(dbConn: Connection, groupBits: Int): Group = {
    val q = dbConn.prepareStatement("SELECT * FROM `groups` WHERE `groups`.`id` = ?")
    q.setInt(1, groupBits)
    val x = groupFromRs(q.executeQuery())
    q.close()
    x
  }

  def setGroupForUser(dbConn: Connection, userId: Int, groupBits: Int) = {
    val stmt = dbConn.prepareStatement("UPDATE `clients` SET `group_bits` = ? WHERE `id` = ?")
    stmt.setInt(1, groupBits)
    stmt.setInt(2, userId)
    stmt.execute()
    stmt.close()
  }
}