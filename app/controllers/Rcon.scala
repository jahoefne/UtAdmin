package controllers

import models.{Ranks, MongoLogger, UtAdminUser}
import models.ChatModel._
import play.api.Logger
import securesocial.core.RuntimeEnvironment

/**
  * REST services for using rcon commands
  */
class Rcon(override implicit val env: RuntimeEnvironment[UtAdminUser])
  extends securesocial.core.SecureSocial[UtAdminUser] {

  val log = Logger(this getClass() getName())
  val server = UtServer

  /** Restart the server by sending a command using ssh */
  def restartServer = SecuredAction {
    request =>
      MongoLogger.logAction(request.user, "Restarting Urt Server!")

      jassh.SSH.once(UtServer.sshIp, UtServer.sshUser, UtServer.sshPass) {
        ssh =>
          print(ssh.execute(UtServer.restartServerCmd))
      }
      Ok("Done.")
  }

  def restartB3() = SecuredAction {
    request =>
      MongoLogger.logAction(request.user, "Restarting Urt B3!")

      jassh.SSH.once(UtServer.sshIp, UtServer.sshUser, UtServer.sshPass) {
        ssh =>
          print(ssh.execute(UtServer.restartB3Cmd))
      }
      Ok("Done.")
  }

  def startServerDemo(player: Int, name: String) = SecuredAction {
    request =>
      MongoLogger.logAction(request.user, "Starting server demo for player " + player)
      server.rcon.rcon("startserverdemo " + player)
      Redirect("/")
  }

  def stopServerDemo(player: Int, name: String) = SecuredAction {
    request =>
      MongoLogger.logAction(request.user, "Stopping server demo for player " + player)
      server.rcon.rcon("stopserverdemo" + player)
      Redirect("/")
  }

  def slap(player: Int, name: String) = SecuredAction {
    request =>
      MongoLogger.logAction(request.user, "Slapping player " + player)
      sendMsg(s"!slap $name", request.user)
      server.rcon.rcon("slap " + player)
      Redirect("/")
  }

  def kill(player: Int, name: String) = SecuredAction {
    request =>
      if (request.user.rank != Ranks.Mod) {
        MongoLogger.logAction(request.user, "Killing player " + player)
        sendMsg(s"!kill $name", request.user)
        server.rcon.rcon("smite " + player)
        Redirect("/")
      } else {
        Unauthorized("You are not allowed to do this")
      }
  }

  def nuke(player: Int, name: String) = SecuredAction {
    request =>
      if (request.user.rank != Ranks.Mod) {
        MongoLogger.logAction(request.user, "Nukeing player " + player)
        sendMsg(s"!nuke $name", request.user)
        server.rcon.rcon("nuke " + player)
        Redirect("/")
      } else {
        Unauthorized("You are not allowed to do this")
      }

  }

  def forceRed(player: Int, name: String) = force(player, "red", name)

  def forceBlue(player: Int, name: String) = force(player, "blue", name)

  def forceSpec(player: Int, name: String) = force(player, "spectator", name)

  private def force(player: Int, team: String, name: String) = SecuredAction {
    request =>
      MongoLogger.logAction(request.user, "Force player " + player)
      sendMsg(s"!force $name $team", request.user)
      server.rcon.rcon("forceteam " + player + " " + team)
      Redirect("/")
  }

  def privateMessage(receiverSlot: Int, text: String, b3Id: Int, receiverName: String) = SecuredAction { request =>
    MongoLogger.logAction(request.user, s"PM TO : $receiverSlot text: $text")
    val toSend = "^2" + request.user.main.userId + "^7: " + text
    server.rcon.rcon(s"tell $receiverSlot $toSend")
    ChatActions.insertMsg(
      adminName = request.user.main.userId,
      message = text,
      msgType = "PM",
      adminId = request.user.b3Id,
      targetId = Some(b3Id),
      targetName = Some(receiverName)
    )
    Ok("Sent!.")
  }

  def sendMsg(text: String, user: UtAdminUser): Unit = {
    val toSend = "^2" + user.main.userId + "^7: " + text
    server.rcon.rcon("say " + toSend)
    ChatActions.insertMsg(
      adminName = user.main.userId,
      message = text,
      adminId = user.b3Id)
  }

  def say(text: String) = SecuredAction { request =>
    MongoLogger.logAction(request.user, "Saying: " + text)
    sendMsg(text, request.user)
    Ok("Sent!.")
  }

  def kick(id: Int, name: String) = SecuredAction { request =>
    if (request.user.rank != Ranks.Mod) {
      MongoLogger.logAction(request.user, s"Kicking Player $name Id= $id")
      sendMsg(s"!kick $name", request.user)
      server.rcon.rcon("kick " + id)
      Redirect(routes.Application.index())
    } else {
      Unauthorized("You are not allowed to do that")
    }
  }
}
