/**
  * This code is pretty dirty and needs to be cleaned up
  */
package models

import java.sql.Connection

import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Json
import scalikejdbc._

/*
case class B3User(name: String,
                  ip: String,
                  id: Int,
                  connections: Int,
                  guid: String,
                  pbid: String,
                  group: Group,
                  firstSeen: DateTime,
                  lastSeen: DateTime)
*/
case class OnlineUser(n: String,
                      ip: String,
                      id: Int,
                      gr: Group,
                      t: String,
                      sc: Int,
                      jo: DateTime,
                      sId: Int,
                      xlrId: Int,
                      c: Option[String], // country
                      cCd: Option[String], // code
                      ccIp: Option[String] // checked ip
                     )

object OnlineUser {
  val log = Logger(this getClass() getName())

  object Formatters {
    implicit val groupFormat = Json.format[Group]
    implicit val userFormat = Json.format[OnlineUser]
  }
/*
  def fromResultSet(set: WrappedResultSet): B3User =  B3User(
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
    )*/
}
