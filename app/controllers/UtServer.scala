package controllers

import models.{RconConnection, B3DatabaseConnection}
import play.api.Logger

object UtServer {
  val name = "NAME_OF_YOUR_GAME_SERVER"

  /** Set up the b3 Database connection */
  val b3 = B3DatabaseConnection(
    ip = "MYSQL_IP",
    username = "MYSQL_USER",
    password = "MYSQL_PASSWORD",
    dbName = "B3_DB_NAME")

  val rcon = new RconConnection(server = "GAMESERVER_IP", rconPassword = "RCON_PASSWORD", port = 27960)

  /**
    * This is for restarting the server using an ssh connection and a command to restart the server
    */
  val sshIp = "SSH_IP"
  val sshPass = "SSH_PASS"
  val sshUser = "SSH_USER"
  val restartCmd = "e.g. screen -S Server -X quit;  screen -m -d -S sh startServer.sh"


  val log = Logger(this getClass() getName())
  log.debug("Initalizing server:"+name)
  b3.connect()
}
