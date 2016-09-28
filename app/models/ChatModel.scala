package models

import org.joda.time.DateTime
import play.api.libs.json.Json
import scalikejdbc.WrappedResultSet
import scalikejdbc._

object Teams extends Enumeration {
  val unknown = Value(-1, "green")
  val free = Value(0, "green")
  val spec = Value(1, "grey")
  val red = Value(2, "red")
  val blue = Value(3, "blue")
  val onlineAdmin = Value(42, "green")
}

object ChatModel {
  case class Message(id: Int,
                     timeStamp: DateTime,
                     scope: String,
                     userId: Int,
                     userName: String,
                     userTeam: String,
                     txt: String,
                     targetId: Option[Int],
                     targetName: Option[String],
                     targetTeam: Option[String],
                     country: Option[String],
                     countryCode: Option[String],
                     checkedIp: Option[String]) {
  }


  object Message extends SQLSyntaxSupport[Message] {

    object Formatters {
      implicit val authorFormat = Json.format[Message]
    }



    private case class Country(code: Option[String], name: Option[String])
/*
    private def updateCountry(msg: Message): OnlineUser = DB autoCommit { implicit session =>
      val c =
        sql"""SELECT * FROM ip2nationCountries c, ip2nation i WHERE i.ip < INET_ATON(${user.ip}) AND c.code = i.country ORDER BY i.ip DESC LIMIT 0,1"""
          .map(rs => new Country(rs.stringOpt("iso_code_2"), rs.stringOpt("iso_country"))).single().apply()
      sql"""INSERT INTO
             countries (id, checked_ip, country_code, country_name)
             VALUES (${msg.clientId}, ${msg..ip}, ${c.get.code}, ${c.get.name})
             ON DUPLICATE KEY UPDATE checked_ip= ${user.ip} , country_code = ${c.get.code}, country_name = ${c.get.name}""".execute.apply()
      user.copy(country = c.get.name, countryCode = c.get.code, checkedIp = Some(user.ip))

    */


    def fromRS(rs: WrappedResultSet): Message =
      Message(
        rs.int("id"),
        new DateTime(rs.long("msg_time") * 1000L),
        rs.string("msg_type"),
        rs.int("client_id"),
        rs.string("client_name"), {
          val t = rs.underlying.getInt("client_team")
          Teams.apply(t).toString
        },
        rs.string("msg"),
        rs.intOpt("target_id"),
        rs.stringOpt("target_name"), {
          val t = rs.underlying.getInt("target_team")
          Some(Teams.apply(t).toString)
        },
        country = rs.stringOpt("country_name"),
        countryCode = rs.stringOpt("country_code"),
        checkedIp = rs.stringOpt("checked_ip"))
  }
}
