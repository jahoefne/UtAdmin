/**
  * This code is pretty dirty and needs to be cleaned up
  */
package models

import java.sql.Connection

import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Json
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
                      team: String,
                      score: Int,
                      joined: DateTime,
                      serverId: Int,
                      xlrId: Int,
                      country: Option[String],
                      countryCode: Option[String],
                      countryCheckedIp: Option[String])

object OnlineUser {
  val log = Logger(this getClass() getName())

  object Formatters {
    implicit val groupFormat = Json.format[Group]
    implicit val authorFormat = Json.format[OnlineUser]
  }

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
    )
}
