import play.PlayImport._
import PlayKeys._
import play.twirl.sbt.Import.TwirlKeys

name := """UtAdmin"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(play.PlayScala)

scalaVersion := "2.11.7"

routesImport ++= Seq("scala.language.reflectiveCalls")

scalacOptions := Seq("-encoding", "UTF-8", "-Xlint", "-deprecation", "-unchecked", "-feature", "-language:implicitConversions")

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "SQLite-JDBC Repository" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

resolvers += "JAnalyse Repository" at "http://www.janalyse.fr/repository/"

resolvers += "Madoushi sbt-plugins" at "https://dl.bintray.com/madoushi/sbt-plugins/"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  javaCore,
  ws,
  "ws.securesocial" % "securesocial_2.11" % "3.0-M3",
  "joda-time" % "joda-time" % "2.7",
  "com.novus" %% "salat" % "1.9.9",
  "org.scalikejdbc" %% "scalikejdbc" % "2.3.5",
  "mysql" % "mysql-connector-java" % "5.1.38",
  "org.mongodb" %% "casbah" % "2.7.3",
  "fr.janalyse"   %% "janalyse-ssh" % "0.9.16" % "compile",
  "javax.inject" % "javax.inject" % "1",
  "org.apache.commons" % "commons-parent" % "39"
)

