package models

import java.sql.{Connection, DriverManager, ResultSet}
import org.joda.time.DateTime
import play.api.Logger

case class B3DatabaseConnection(ip: String = "127.0.0.1",
                                username: String = "root",
                                password: String = "root",
                                dbName: String = "b3bot") {
  val log = Logger(this getClass() getName())

  classOf[com.mysql.jdbc.Driver]
  var connection: Connection = null
  val driver = "com.mysql.jdbc.Driver"
  val url = "jdbc:mysql://" + ip + "/" + dbName

  def connect(): Unit = {
    log.debug("\tConnection to B3 Database " + dbName + " with user " + username + " on server" + ip)
    try {
      connection = DriverManager.getConnection(url, username, password)
    }
  }
}
