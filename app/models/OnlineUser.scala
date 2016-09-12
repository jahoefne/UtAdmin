/**
  * This code is pretty dirty and needs to be cleaned up
  */
package models

import java.sql.Connection

import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Json
import scalikejdbc._


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
}
