package controllers

import org.joda.time.DateTime
import scalikejdbc._

import models.OnlineUser.Formatters._
import models._
import securesocial.core.RuntimeEnvironment
import play.api.libs.json._


/**
  * Created by jahoefne on 03/09/16.
  */
class UserController(override implicit val env: RuntimeEnvironment[UtAdminUser]) extends securesocial.core.SecureSocial[UtAdminUser] {
  def getJsonOnlinePlayers() = SecuredAction {
    implicit request =>
      val onlinePlayers = Json.toJson(UserController.listOnlineUsers.sortWith((a, b) => {
        a.team > b.team || a.team == b.team && a.score > b.score
      }))
      Ok(onlinePlayers)
  }
}


object UserController {


  private case class Country(code: Option[String], name: Option[String])

  private def findCountry(user: OnlineUser): OnlineUser = DB autoCommit { implicit session =>
    user.country match {
      case None => {
        println("Country Table Cache Miss!")
        val c =
          sql"""SELECT * FROM ip2nationCountries c, ip2nation i WHERE i.ip < INET_ATON(${user.ip}) AND c.code = i.country ORDER BY i.ip DESC LIMIT 0,1"""
            .map(rs => new Country(rs.stringOpt("iso_code_2"), rs.stringOpt("iso_country"))).single().apply()
        sql"""INSERT INTO
             countries (id, checked_ip, country_code, country_name)
             VALUES (${user.id}, ${user.ip}, ${c.get.code}, ${c.get.name})
             ON DUPLICATE KEY UPDATE checked_ip= ${user.ip} , country_code = ${c.get.code}, country_name = ${c.get.name}""".execute.apply()
        user.copy(country = c.get.name, countryCode = c.get.code, countryCheckedIp = Some(user.ip))
      }
      case _ => user
    }
  }

  def listOnlineUsers: Seq[OnlineUser] = DB readOnly {
    implicit session =>
      sql"""SELECT current_clients.*, clients.time_edit, xlr_playerstats.id AS xlr_id,
                   groups.name AS group_name, groups.id AS group_bits, groups.level AS group_level,
                    c.*
            FROM clients, xlr_playerstats, groups, current_clients
            LEFT JOIN countries c ON (current_clients.DBID = c.id)
            WHERE current_clients.guid = clients.guid
            AND xlr_playerstats.client_id = current_clients.dbid
            AND current_clients.level = groups.level
      """.map(rs => OnlineUser(
        name = rs.string("ColorName").dropRight(2),
        ip = rs.string("ip"),
        id = rs.int("DBID"),
        connections = rs.int("Connections"),
        group = Group(
          bits = rs.int("group_bits"),
          level = rs.int("group_level"),
          name = rs.string("group_name")),
        score = rs.int("Score"),
        team = {
          val t = rs.underlying.getInt("Team")
          Teams.apply(t).toString
        },
        joined = new DateTime(rs.long("time_edit") * 1000L),
        serverId = rs.int("CID"),
        xlrId = rs.int("xlr_id"),
        country = rs.stringOpt("country_name"),
        countryCode = rs.stringOpt("country_code"),
        countryCheckedIp = rs.stringOpt("checked_ip")
      )).list().apply().toSeq.map(findCountry)
  }
}
