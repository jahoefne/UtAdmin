package models

import com.mongodb.DBObject
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.novus.salat.Context
import com.novus.salat._
import play.api.{Logger, Play}
import securesocial.core.{PasswordInfo, BasicProfile}
import securesocial.core.providers.MailToken
import securesocial.core.services.{SaveMode, UserService}
import scala.concurrent.Future

case class UtAdminUser(rank: Int, main: BasicProfile,  b3Id:Int = 0)

object Ranks extends Enumeration {
  val God = 0
  val Admin = 1
  val Mod = 2
}

/**
  * This code implements the methods used by the user-framework used for the admin accounts
  */
object UtAdminUserService extends UserService[UtAdminUser] {

  import com.mongodb.casbah.commons.conversions.scala._

  RegisterJodaTimeConversionHelpers()

  val conn = MongoClient("127.0.0.1", 27017)
  val db = conn("UtAdmin")
  val users = db("Users")
  val tokens = db("Tokens")

  val log = Logger(this getClass() getName())

  def getAllUsers: Seq[UtAdminUser] = {
    log.info("Querying all admin accounts")
    var allUsers = Seq.empty[UtAdminUser]
    users.find(MongoDBObject("_id" -> MongoDBObject("$exists" -> true))).foreach((u: DBObject)
    => allUsers :+= fromMongoDbObject(u)
    )
    log.info("Done querying all admin accounts!")
    allUsers
  }

  def deleteUser(id: String): Boolean = {
    log.info("Trying to delete user with id - " + id)
    users.find((obj: DBObject) => {
      log.debug("Checking - " + obj.main.userId + " against id " + id + "  is " + (obj.main.userId == id));
      obj.main.userId == id
    }) match {
      case Some(u) =>
        users.remove(u)
        log.info("Deleted!")
        true
      case _ =>
        log.warn("User not found!!")
        false
    }
  }

  override def find(providerId: String, userId: String): Future[Option[BasicProfile]] = {
    log.info("Find " + providerId + " " + userId)
    users.find((obj: DBObject) => {
      log.debug("Checking :" + obj.main.userId + "  " + obj.main.providerId)
      obj.main.providerId == providerId && obj.main.userId == userId
    }) match {
      case Some(u) =>
        log.info("Found user:" + u.main.email)
        Future.successful(Some(u.main))
      case _ =>
        log.warn("User not found!")
        Future.successful(None)
    }
  }

  override def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] = {
    log.info("Find by email and provider " + email + " " + providerId)
    users.find((obj: DBObject) => obj.main.email.getOrElse("") == email && obj.main.providerId == providerId) match {
      case Some(u) => Future.successful(Some(u.main))
      case _ => Future.successful(None)
    }
  }

  /** Delete Token */
  override def deleteToken(uuid: String): Future[Option[MailToken]] = {
    val query = MongoDBObject("uuid" -> uuid)
    val token = tokenFromDbObject(tokens.findOne(query).get)
    tokens.remove(query)
    Future.successful(Some(token))
  }

  /** We do not support linking of profiles */
  override def link(current: UtAdminUser, to: BasicProfile): Future[UtAdminUser] = Future.successful(current)

  override def passwordInfoFor(user: UtAdminUser): Future[Option[PasswordInfo]] =
    users.find((obj: DBObject) => obj.main.providerId == user.main.providerId && obj.main.userId == user.main.userId) match {
      case Some(u) => Future.successful(u.main.passwordInfo)
      case _ => Future.successful(None)
    }

  def insertUtAdminUser(user: UtAdminUser) = {
    val found = users.find((obj: DBObject) =>
      obj.main.providerId == user.main.providerId && obj.main.userId == user.main.userId)
    found match {
      case None => users.save(user)
      case _ => users.update(found.get, user, upsert = true)
    }
  }

  /** Save User Data, depending on 'mode' data has to be inserted or updated */
  override def save(profile: BasicProfile, mode: SaveMode): Future[UtAdminUser] = {
    mode match {

      case SaveMode.SignUp =>
        val newUser = UtAdminUser(3, profile,0)
        users.save(newUser)
        Future.successful(newUser)

      case SaveMode.LoggedIn =>
        val found = users.find((obj: DBObject) => obj.main.providerId == profile.providerId && obj.main.userId == profile.userId)

        found match {
          case Some(dbObj) =>
            val updated = new UtAdminUser(dbObj.rank, profile, dbObj.b3Id)
            users.update(dbObj, updated, upsert = true)
            Future.successful(updated)

          case None =>
            val newUser = UtAdminUser(3, profile)
            users.save(newUser)
            Future.successful(newUser)
        }

      case SaveMode.PasswordChange =>
        val found = users.find((obj: DBObject) => obj.main.providerId == profile.providerId && obj.main.userId == profile.userId)
        val updated = new UtAdminUser(found.get.rank, profile)
        users.update(found.get, updated, upsert = true)
        Future.successful(updated)
    }
  }

  /** delete all expired tokens */
  override def deleteExpiredTokens(): Unit = {
    for (obj <- tokens.iterator) {
      if (tokenFromDbObject(obj).isExpired)
        tokens.remove(obj)
    }
  }

  /** Update the password info for user */
  override def updatePasswordInfo(user: UtAdminUser, info: PasswordInfo): Future[Option[BasicProfile]] = {
    val found = users.find((obj: DBObject) =>
      obj.main.providerId == user.main.providerId && obj.main.userId == user.main.userId)
    val updated = found.get.copy(main = found.get.main.copy(passwordInfo = Some(info)))
    users.update(found.get, updated, upsert = true)
    Future.successful(Some(updated.main))
  }

  /** Find Token based on UUID */
  override def findToken(token: String): Future[Option[MailToken]] = {
    tokens.findOne(MongoDBObject("uuid" -> token)) match {
      case Some(t) => Future.successful(Some(tokenFromDbObject(t)))
      case _ => Future.successful(None)
    }
  }

  /** Save a token to the DB */
  override def saveToken(token: MailToken): Future[MailToken] = {
    println(tokenToDbObject(token).toString())
    Future.successful {
      tokens.save(tokenToDbObject(token))
      token
    }
  }

  /** Conversion Helpers, do the Conversion with Salat */
  implicit val ctx = new Context {
    val name = "Custom_Classloader"
  }
  ctx.registerClassLoader(Play.classloader(Play.current))

  /** Implicit conversions from to UserObject */
  implicit def User2MongoDB(u: UtAdminUser): DBObject = grater[UtAdminUser].asDBObject(u)

  implicit def fromMongoDbObject(o: DBObject): UtAdminUser = grater[UtAdminUser].asObject(o)

  /** Explicit conversion for tokens because implicit methods must be unique per context */
  def tokenToDbObject(t: MailToken): DBObject = grater[MailToken].asDBObject(t)

  def tokenFromDbObject(o: DBObject): MailToken = grater[MailToken].asObject(o)
}
