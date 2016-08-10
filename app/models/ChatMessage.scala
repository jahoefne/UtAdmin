package models

import java.sql.{Connection, ResultSet}

import org.joda.time.DateTime
import play.api.Logger
import scalikejdbc._

case class ChatMessage(msgId: Int,
                       timestamp: DateTime,
                       msgType: String,
                       userId: Int,
                       username: String,
                       userTeam: Int,
                       msg: String,
                       targetId: Option[Int],
                       targetName: Option[String],
                       targetTeam: Option[Int])

object Teams extends Enumeration {
  val unknown = -1
  val free = 0
  val spec = 1
  val red = 2
  val blue = 3
}

object ChatMessage {
  val log = Logger(this getClass() getName())

  def fromResultSet(set: ResultSet): ChatMessage =
    ChatMessage(
      set.getInt("id"),
      new DateTime(set.getLong("msg_time") * 1000L),
      set.getString("msg_type"),
      set.getInt("client_id"),
      set.getString("client_name"),
      set.getInt("client_team"),
      set.getString("msg"), {
        val res = set.getInt("target_id")
        set.wasNull() match {
          case true => None
          case false => Some(res)
        }
      }, {
        val res = set.getString("target_name")
        set.wasNull() match {
          case true => None
          case false => Some(res)
        }
      }, {
        val res = set.getInt("target_team")
        set.wasNull() match {
          case true => None
          case false => Some(res)
        }
      }
    )


  def getOffsetFor(msgId: Int): Int = DB readOnly { implicit session =>
    sql"SELECT COUNT(*) as count FROM chatlog WHERE msg NOT LIKE 'RADIO %' AND id > $msgId"
      .map(rs => rs.int("count")).first().apply().getOrElse(0)
  }

  def getChatLog(count: Int, offset: Int, userId: Option[Int], includeRadio: Boolean): Seq[ChatMessage] = DB readOnly {
    implicit session =>
      sql"""SELECT * FROM chatlog WHERE
        (${userId.isDefined} = FALSE OR client_id = ${userId.getOrElse(-1)})
        ORDER BY msg_time DESC LIMIT $offset, 200"""
        .map(rs => {
          ChatMessage(
            rs.int("id"),
            new DateTime(rs.long("msg_time") * 1000L),
            rs.string("msg_type"),
            rs.int("client_id"),
            rs.string("client_name"),
            rs.underlying.getInt("client_team"),
            rs.string("msg"),
            rs.intOpt("target_id"),
            rs.stringOpt("target_name"),
            Some(rs.underlying.getInt("target_team"))
          )
        }
        ).list().apply.toSeq.filter(includeRadio || !_.msg.startsWith("RADIO")).take(count);
  }

  def insertChatMessage(adminName: String,
                        message: String,
                        msgType: String = "ALL",
                        adminId: Int,
                        targetId: Option[Int] = None,
                        targetName: Option[String] = None): Unit = DB localTx { implicit session =>

    sql"""INSERT INTO  b3bot.chatlog (id , msg_time, msg_type , client_id , client_name,
           client_team, msg , target_id , target_name, target_team )
           VALUES ( NULL ,
      ${DateTime.now().getMillis / 1000},
           $msgType,  $adminId, $adminName,  '42', $message,
       $targetId, ${targetName.getOrElse("").dropRight(2)} , NULL )""".execute().apply()
  }
}
