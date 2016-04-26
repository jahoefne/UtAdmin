package models

import java.sql.{JDBCType, Connection, ResultSet}

import org.joda.time.DateTime
import play.api.Logger

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


  def getOffsetFor(connection: Connection, msgId: Int): Int = {
    val query = connection.prepareStatement("SELECT COUNT(*) as count FROM `chatlog` " +
      "WHERE `chatlog`.`msg` NOT LIKE  'RADIO %' AND `chatlog`.`id` > ?")
    query.setInt(1, msgId)
    var chatHistory = Seq.empty[ChatMessage]
    val resultSet = query.executeQuery()

    resultSet.next()
    val offset = resultSet.getInt("count")
    println("Calculated offset ")
    query.close()
    resultSet.close()
    offset
  }

  def getChatLog(connection: Connection, count: Int, offset: Int, userId: Option[Int], includeRadio: Boolean): Seq[ChatMessage] = {
    log.debug("Querying Chatlog!")
    val query = connection.prepareStatement("SELECT * FROM `chatlog` " +
      "WHERE `chatlog`.`msg` NOT LIKE " + {
      if (!includeRadio) {
        "'RADIO %' "
      } else {
        "' '"
      }
    }
      + {
      if (userId.isDefined) {
        " AND `chatlog`.`client_id` = ? "
      } else {
        " "
      }
    } +
      "ORDER BY `chatlog`.`msg_time` DESC LIMIT ?, ?")

    if (userId.isDefined) {
      query.setInt(1, userId.get)
      query.setInt(2, offset)
      query.setInt(3, count)
    } else {
      query.setInt(1, offset)
      query.setInt(2, count)
    }

    var chatHistory = Seq.empty[ChatMessage]
    val resultSet = query.executeQuery()
    while (resultSet.next())
      chatHistory :+= ChatMessage.fromResultSet(resultSet)

    query.close()
    resultSet.close()
    chatHistory
  }

  def insertChatMessage(connection: Connection,
                        adminName: String,
                        message: String,
                        msgType: String = "ALL",
                        adminId: Int,
                        targetId: Option[Int] = None,
                        targetName: Option[String] = None): Unit = {
    val ins = connection.prepareStatement(
      s"""INSERT INTO  `b3bot`.`chatlog` ( `id` , `msg_time`, `msg_type` , `client_id` , `client_name`,
           `client_team`, `msg` , `target_id` , `target_name`, `target_team` ) VALUES ( NULL , ?,
           '$msgType',  '$adminId', ?,  '42', ?, ? , ? , NULL )""")

    ins.setLong(1, DateTime.now().getMillis / 1000)
    ins.setString(2, adminName)
    ins.setString(3, message)
    targetId match {
      case Some(id) => ins.setInt(4, id)
      case _ => ins.setNull(4, java.sql.Types.INTEGER);
    }
    targetName match {
      case Some(name) => ins.setString(5, name.dropRight(2))
      case _ => ins.setNull(5, java.sql.Types.VARCHAR);
    }


    if (!ins.execute()) {
      println("Error while inserting chatmessage!")
    }
    ins.close()
  }
}
