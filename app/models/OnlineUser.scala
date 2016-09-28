/**
  * This code is pretty dirty and needs to be cleaned up
  */
package models

import java.sql.Connection

import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Json
import scalikejdbc._


case class OnlineUser(name: String,
                      ip: String,
                      id: Int,
                      group: Group,
                      team: String,
                      score: Int,
                      joined: DateTime,
                      serverId: Int,
                      xlrId: Int,
                      country: Option[String], // country
                      countryCode: Option[String], // code
                      checkedIp: Option[String] // checked ip
                     )

object OnlineUser {
  val log = Logger(this getClass() getName())

  object Formatters {
    implicit val groupFormat = Json.format[Group]
    implicit val userFormat = Json.format[OnlineUser]
  }
}
