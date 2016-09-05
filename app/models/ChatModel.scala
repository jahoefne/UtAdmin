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
                     targetTeam: Option[String]) {
  }


  object Message extends SQLSyntaxSupport[Message] {

    object Formatters {
      implicit val authorFormat = Json.format[Message]
    }

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
        })
  }
}
