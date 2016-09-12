package controllers

import controllers.UserController.QueryUser
import org.joda.time.DateTime
import play.twirl.api.Html
import scalikejdbc._

import models.OnlineUser.Formatters._
import models.User.Formatters._
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
        a.t > b.t || a.t == b.t && a.sc > b.sc
      }))
      Ok(onlinePlayers)
  }

  def userJsonById(id: Int) = SecuredAction { request =>
    Ok(Json.toJson(User.UserInfo.getUserByB3Id(id)))
  }

  def usersJsonByIp(ip: Option[String], groupBits: Option[Int], count: Int, page: Int) = SecuredAction { request =>
    implicit val queryUserFormat = Json.format[QueryUser]
    Ok(Json.toJson(UserController.getUsersByIp(ip, groupBits, count, page)))
  }

  def usersJsonByName(ip: Option[String], groupBits: Option[Int], count: Int, page: Int) = SecuredAction { request =>
    implicit val queryUserFormat = Json.format[QueryUser]
    Ok(Json.toJson(UserController.getUsersByName(ip, groupBits, count, page)))
  }

  def getStatusTemplateHtml() = SecuredAction { request =>
    Ok(views.html.status.render(request.user))
  }

  def getUserTemplatetHtml() = SecuredAction { request =>
    Ok(views.html.user.render(request.user))
  }

  def getUsersTemplateHtml() = SecuredAction { request =>
    Ok(views.html.users.render())
  }

  def changeGroupOfUser(userId: Int, groupBits: Int) = SecuredAction {
    request =>
      MongoLogger.logAction(request.user, "Changing group for user " + userId + " to bits: " + groupBits)
      request.user.rank match {
        case Ranks.God =>
          B3GroupController.setGroupForUser(userId, groupBits)
          Redirect(request.request.headers.get("referer").getOrElse("/"))
        case _ =>
          Unauthorized("You have no power here")
      }
  }

  def setXlrVisibility(b3Id: Int, visibility: Boolean) = SecuredAction { request =>
    if (request.user.rank == Ranks.Admin || request.user.rank == Ranks.God) {
      User.UserInfo.setXlrStatsVisibility(b3Id, visibility)
      Ok("Done")
    }else{
      BadRequest("")
    }
  }
}

object UserController {

  case class QueryUser(name: String, id: Int, lastSeen: Long)

  object Formatters {
    implicit val queryUserFormat = Json.format[QueryUser]
  }

  private def getUsersByName(nameOpt: Option[String], groupBits: Option[Int], count: Int, page: Int): Seq[QueryUser] = DB readOnly { implicit session =>
    val offset = count * page

    /* Construct where clause based on parameter options */
    val where = nameOpt match {
      case Some(name) =>
        groupBits match {
          case Some(bits) =>
            sqls.where(sqls.eq(sqls"clients.group_bits", bits).and(sqls.like(sqls"aliases.alias", "%" + name + "%").or.like(sqls"clients.name", "%" + name + "%")))
          case _ =>
            sqls.where(sqls.like(sqls"clients.name", "%" + name + "%").or.like(sqls"aliases.alias", "%" + name + "%"))
        }
      case _ =>
        groupBits match {
          case Some(bits) =>
            sqls.where(sqls.eq(sqls"clients.group_bits", bits))
          case _ =>
            sqls.empty
        }
    }

    sql"""SELECT clients.name, clients.id, clients.time_edit
          FROM clients LEFT OUTER JOIN aliases ON aliases.client_id = clients.id
          $where
          GROUP BY id ORDER BY clients.time_edit DESC LIMIT $count OFFSET $offset"""
      .map(rs => QueryUser(name = rs.string("name"), id = rs.int("id"), lastSeen = rs.long("time_edit"))).list.apply().toSeq
  }


  private def getUsersByIp(ipOpt: Option[String], groupBits: Option[Int], count: Int, page: Int): Seq[QueryUser] = DB readOnly { implicit session =>
    val offset = count * page

    /* Construct where clause based on parameter options */
    val where = ipOpt match {
      case Some(ip) =>
        groupBits match {
          case Some(bits) =>
            sqls.where(sqls.eq(sqls"clients.group_bits", bits).and(sqls.like(sqls"ipaliases.ip", ip + "%").or.like(sqls"clients.ip", ip + "%")))
          case _ =>
            sqls.where(sqls.like(sqls"ipaliases.ip", ip + "%").or.like(sqls"clients.ip", ip + "%"))
        }
      case _ =>
        groupBits match {
          case Some(bits) =>
            sqls.where(sqls.eq(sqls"clients.group_bits", bits))
          case _ =>
            sqls.empty
        }
    }

    sql"""SELECT clients.name, clients.id, clients.time_edit
          FROM clients LEFT OUTER JOIN ipaliases ON ipaliases.client_id = clients.id
          $where
          GROUP BY id ORDER BY clients.time_edit DESC LIMIT $count OFFSET $offset"""
      .map(rs => QueryUser(name = rs.string("name"), id = rs.int("id"), lastSeen = rs.long("time_edit"))).list.apply().toSeq
  }

  private case class Country(code: Option[String], name: Option[String])

  private def updateCountry(user: OnlineUser): OnlineUser = DB autoCommit { implicit session =>
    val c =
      sql"""SELECT * FROM ip2nationCountries c, ip2nation i WHERE i.ip < INET_ATON(${user.ip}) AND c.code = i.country ORDER BY i.ip DESC LIMIT 0,1"""
        .map(rs => new Country(rs.stringOpt("iso_code_2"), rs.stringOpt("iso_country"))).single().apply()
    sql"""INSERT INTO
             countries (id, checked_ip, country_code, country_name)
             VALUES (${user.id}, ${user.ip}, ${c.get.code}, ${c.get.name})
             ON DUPLICATE KEY UPDATE checked_ip= ${user.ip} , country_code = ${c.get.code}, country_name = ${c.get.name}""".execute.apply()
    user.copy(c = c.get.name, cCd = c.get.code, ccIp = Some(user.ip))

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
      """.map(rs =>
        OnlineUser(
          n = rs.string("ColorName").dropRight(2),
          ip = rs.string("ip"),
          id = rs.int("DBID"),
          gr = Group(
            bits = rs.int("group_bits"),
            level = rs.int("group_level"),
            name = rs.string("group_name")),
          sc = rs.int("Score"),
          t = {
            val t = rs.underlying.getInt("Team")
            Teams.apply(t).toString
          },
          jo = new DateTime(rs.long("time_edit") * 1000L),
          sId = rs.int("CID"),
          xlrId = rs.int("xlr_id"),
          c = rs.stringOpt("country_name"),
          cCd = rs.stringOpt("country_code"),
          ccIp = rs.stringOpt("checked_ip")
        )).list().apply().toSeq
        .map(x => if (x.c.isEmpty || x.ip != x.ccIp.getOrElse("")) updateCountry(x) else x)
  }
}
