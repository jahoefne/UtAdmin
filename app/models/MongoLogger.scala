package models

import com.mongodb.DBObject
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoClient
import com.novus.salat.Context
import org.joda.time.DateTime
import play.api.Play

import com.novus.salat._

import scala.concurrent.Future

case class LogEntry(timestamp: DateTime,
                    user: UtAdminUser,
                    message: String)

object MongoLogger {

  import com.mongodb.casbah.commons.conversions.scala._

  import UtAdminUserService.ctx
  com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers()

  val conn = MongoClient("127.0.0.1", 27017)
  val db = conn("UtAdmin")
  val logs = db("Logs")
  val loggedIn = db("LoggedInUsers")

  def updateOnline(user: UtAdminUser) = {
    loggedIn.update(
      MongoDBObject("username" -> user.main.userId),
      MongoDBObject("username" -> user.main.userId, "lastAction" -> DateTime.now().getMillis),
      upsert = true
    )
  }

  def getLastOnlineInEchelonFor(username: String): DateTime ={
      loggedIn.findOne(MongoDBObject("username" -> username)) match {
        case Some(found) => new DateTime(found.get("lastAction").asInstanceOf[Long]);
        case _ => new DateTime(0)
      }
  }

  def getLoggedInUsers: Seq[String] = {
    var online = Seq.empty[String]
    loggedIn.find(MongoDBObject("_id" -> MongoDBObject("$exists" -> true))).foreach((u: DBObject)
    => {
      if (new DateTime(u.get("lastAction").asInstanceOf[Long])
        .isAfter(DateTime.now().minusMinutes(5)))
        online :+= u.get("username").asInstanceOf[String]
    }
    )
    online
  }

  def logAction(user: UtAdminUser, action: String) = Future.successful {
    updateOnline(user)
    val entry = LogEntry(DateTime.now(), user, action)
    logs.insert(grater[LogEntry].asDBObject(entry))
  }

  def readLogs(): Seq[LogEntry] = {
    var allLogs = Seq.empty[LogEntry]
    logs.find(MongoDBObject("_id" -> MongoDBObject("$exists" -> true))).foreach((u: DBObject)
    => allLogs :+= grater[LogEntry].asObject(u)
    )
    allLogs.sortWith((a: LogEntry, b: LogEntry) => a.timestamp.isAfter(b.timestamp))
  }

}
