package models

import java.net.{SocketTimeoutException, DatagramPacket, DatagramSocket}
import java.nio.charset.Charset
import org.joda.time.DateTime

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import play.api.Logger
import language._

case class RconPlayer(name: String,
                      ip: String,
                      num: Int,
                      ping: Int,
                      score: Int)

class RconConnection(server: String, rconPassword: String, port: Int = 27960) {

  val log = Logger(this getClass() getName())

  private val socket = new DatagramSocket

  def sendCommand(command: String): Seq[String] = {
    val magic = Array(0xff, 0xff, 0xff, 0xff).map(_.toByte)
    val rawCommand = magic ++ command.getBytes ++ "\n".getBytes

    val packet = new DatagramPacket(
      rawCommand,
      rawCommand.length,
      java.net.InetAddress.getByName(server),
      port)
    socket.send(packet)

    /** The server might split big responses in multiple packages. E.g. if the server if full and we query
      * /rcon status.
      * There's no clean way to determine if the received package is a split one or not. Therefore we must use a timeout
      * and hope that all packages have been received when it occurs.
      * Ugly code but seems to work.
      */
    socket.setSoTimeout(1000)
    var loop = true
    var received = Seq.empty[String]
    var packetNum = 0
    while (loop) {
      try {
        val buf = Array.ofDim[Byte](4096)
        val recPack = new DatagramPacket(buf, buf.length)
        socket.receive(recPack)
        val rec = new String(recPack.getData)

        if (rec.length == 0)
          loop = false
        else {
          packetNum += 1
          val allLines = rec.lines.toList.drop(
          {
            if (packetNum == 1) {
              2
            } else {
              1
            }
          })

          for (str <- allLines) {
            if (str == allLines.head && packetNum != 1) {
              val conc = received.last.concat(str.replaceAll("\00+", ""))
              received = received.dropRight(1)
              received :+= conc
            } else {
              received :+= str.replaceAll("\00+", "")
            }
          }
        };

      } catch {
        case e: SocketTimeoutException =>
          if (received.isEmpty) {
            log.error("No Response from server!")
          }
          loop = false
          received

        case e: Exception =>
          e.printStackTrace()
          loop = false
          received
      }
    }
    received
  }

  def bytesToHex(bytes: List[Byte]) =
    bytes.map { b => String.format("%02X", java.lang.Byte.valueOf(b))}.mkString(" ")

  def rcon(command: String) = sendCommand("rcon %s %s".format(rconPassword, command))

}