package controllers

import controllers.ServerStats.{Player, TopTotalTimePlayers}
import models.UtAdminUser
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, JsValue, JsArray, Json}
import scalikejdbc.DB
import scalikejdbc._
import securesocial.core.RuntimeEnvironment

/**
  * Created by jahoefne on 19/09/16.
  */
class ServerStats(override implicit val env: RuntimeEnvironment[UtAdminUser])
  extends securesocial.core.SecureSocial[UtAdminUser] {

  def serverStats() = SecuredAction {
    implicit request =>
      Ok(views.html.serverStats.render())
  }

  def totalDeaths = SecuredAction { implicit request =>
    /*Ok(Json.toJson(DB readOnly { implicit session =>
      sql"""SELECT SUM(kills), SUM(teamkills), SUM(suicides) FROM `xlr_playerstats` ORDER BY `id` ASC"""
        .map(rs => TotalDeathsJson(rs.int(1), rs.int(2), rs.int(3))).single().apply()
    }))*/
    Ok("Done")
  }


  def timeInfoTemplate() = SecuredAction { implicit request =>
    Ok(views.html.stats.timeInfoTeamplate.render())
  }

  def timeInfoJson(count: Int) = SecuredAction { implicit request =>
    Ok(JsObject(Seq(
      ("totalTime", ServerStats.totalTime()),
      ("topTotalTime", ServerStats.topTotalTime(count)))))
  }
}


object ServerStats {


  case class TotalDeathsJson(kills: Int, teamKills: Int, suicides: Int)

  case class Player(name: String, id: Int)

  case class TopTotalTimePlayers(player: Player, time: Int)

  object Formatters {
    implicit val totalDeathsFormat = Json.format[TotalDeathsJson]
    implicit val playerFormat = Json.format[Player]
    implicit val topTotalTimeFormat = Json.format[TopTotalTimePlayers]
  }

  import Formatters._

  /** Kills and Weaponry Info
    * */
  def totalDeath(): JsValue = DB readOnly { implicit session =>
    Json.toJson(
      sql"""SELECT SUM(kills), SUM(teamkills), SUM(suicides) FROM `xlr_playerstats` ORDER BY `id` ASC"""
        .map(rs => TotalDeathsJson(rs.int(1), rs.int(2), rs.int(3))).single().apply()
    )
  }

  /** Time Info
    * */
  def totalTime(): JsValue = DB readOnly { implicit session =>
    Json.toJson(sql"""SELECT SUM(gone-came) FROM ctime""".map(rs => rs.int(1)).single().apply())
  }

  def topTotalTime(count: Int): JsValue = DB readOnly { implicit session =>
    Json.toJson(
      sql"""SELECT SUM(gone - came), ctime.guid, clients.name, clients.id
                        FROM ctime, clients WHERE clients.guid = ctime.guid
                        GROUP BY 2
                        ORDER BY 1 DESC
                        LIMIT $count"""
        .map(rs => TopTotalTimePlayers(Player(rs.string(3), rs.int(4)), rs.int(1))).list().apply())
  }
}
