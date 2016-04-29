package controllers

import models.{PenaltyController, B3UserController, UtAdminUser}
import play.api.Logger
import play.api.cache.Cached
import play.api.mvc.Action
import securesocial.core.RuntimeEnvironment
import play.api.Play.current

/**
 * These routes can be embedded from another server. This allows to display status and banlist tables in forums or
  * clan-websites.
  * To make this work the origin policy has to be set accordingly
 */
class Public(override implicit val env: RuntimeEnvironment[UtAdminUser])
  extends securesocial.core.SecureSocial[UtAdminUser] {

  val log = Logger(this getClass() getName())
  val server = UtServer

  def preflight(all: String) = Action {
    Ok("").withHeaders("Access-Control-Allow-Origin" -> "*",
      "Allow" -> "*",
      "Access-Control-Allow-Methods" -> "POST, GET, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers" -> "Origin, X-Requested-With, Content-Type, Accept, Referrer, User-Agent");
  }

  def publicStatus() = Cached.status(_ => "onlinePlayersPublic", status = 200,duration =  5) {
    Action {
      Ok(views.html.publicStatus(B3UserController.listOnlineUsers.
        sortWith(_.score > _.score))).withHeaders(
          "Access-Control-Allow-Origin" -> "*",
          "Access-Control-Allow-Methods" -> "POST, GET, OPTIONS, PUT, DELETE",
          "Access-Control-Allow-Headers" -> "x-requested-with,content-type,Cache-Control,Pragma,Date"
        )
    }
  }

  def publicBans() = Cached.status(_ => "publicBans", 200, duration = 300){
    Action {
      Ok(views.html.publicBans(PenaltyController.getPenalties(
        userId = None,
        banOnly = true,
        adminOnly = false,
        activeOnly = true,
        noticeOnly = false))).withHeaders(
          "Access-Control-Allow-Origin" -> "*",
          "Access-Control-Allow-Methods" -> "POST, GET, OPTIONS, PUT, DELETE",
          "Access-Control-Allow-Headers" -> "x-requested-with,content-type,Cache-Control,Pragma,Date"
        )
    }
  }

}
