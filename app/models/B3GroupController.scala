package models

import java.sql.{ResultSet, Connection}
import scalikejdbc._

import controllers.UtServer
import org.joda.time.DateTime

case class Group(bits: Int, level: Int, name: String)

object B3GroupController {

  private def groupFromRs(rs: WrappedResultSet) = Group(
    bits = rs.int("id"),
    level = rs.int("level"),
    name = rs.string("name")
  )

  def getGroups(): Seq[Group] = DB readOnly { implicit session =>
    sql"SELECT * FROM groups".map(groupFromRs).list().apply().toSeq.sortWith(_.level > _.level)
  }

  def getGroupForGroupLevel(i: Int): Group = DB readOnly { implicit session =>
    sql"SELECT * FROM groups WHERE id = $i".map(groupFromRs).single().apply().get
  }

  def setGroupForUser(userId: Int, groupBits: Int) = DB localTx { implicit session =>
    sql"UPDATE clients SET group_bits = $groupBits WHERE id = $userId".execute().apply()
  }
}