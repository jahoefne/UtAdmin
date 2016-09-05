package models

import java.sql.{Connection, DriverManager}
import play.api.Logger
import scalikejdbc._


case class B3DatabaseConnection(ip: String = "127.0.0.1",
                                username: String = "root",
                                password: String = "root",
                                dbName: String = "b3bot") {
  val log = Logger(this getClass() getName())

  classOf[com.mysql.jdbc.Driver]
  val driver = "com.mysql.jdbc.Driver"
  val url = "jdbc:mysql://" + ip + "/" + dbName

  // after loading JDBC drivers
  ConnectionPool.singleton(url, username, password)
  ConnectionPool.add('foo, url, username, password)

  val settings = ConnectionPoolSettings(
    initialSize = 5,
    maxSize = 20,
    connectionTimeoutMillis = 3000L,
    validationQuery = "select 1 from dual")

  // all the connections are released, old connection pool will beabandoned
  ConnectionPool.add('foo, url, username, password, settings)

  DB autoCommit { implicit session =>
    sql"create table if NOT EXISTS countries (id integer primary key, checked_ip varchar(30),country_code varchar(15),country_name varchar(120))".execute.apply()
  }
}
