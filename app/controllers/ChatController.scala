package controllers

import org.joda.time.DateTime
import scalikejdbc._

import models.ChatModel._
import models._
import securesocial.core.RuntimeEnvironment
import play.api.libs.json._
import models.ChatModel.Message.Formatters._


class ChatController(override implicit val env: RuntimeEnvironment[UtAdminUser]) extends securesocial.core.SecureSocial[UtAdminUser] {

  def chatlog() = SecuredAction {
    implicit request =>
      Ok(views.html.chatlog.render())
  }

  /**
    * Universal Chat Querying to specify a number of options to make this method usable for every
    * chat funtionality in UtAdmin
    *
    * @param count the number of messages that should be returned
    * @param page the current page (based on count)
    * @param radio whether radio messages should be included
    * @param userId whether only messages by a given user should be displayed
    * @param convId specify a conversation starting at the given id (return message with convId on page 0)
    * @param queryString only include messages containing the string
    * @param latestId used to filter the messages AFTER all the parameters above where applied - used to only send newer messages to the user
    * @return json array of messages
    */
  def getJsonChat(count: Int, page: Int, radio: Boolean, userId: Option[Int], convId: Option[Int], queryString: Option[String], latestId: Int) = SecuredAction {
    request =>
      val msgs = Json.toJson(ChatActions.queryChat(count, page, radio, userId, convId, queryString).filter(_.id > latestId).sortWith(_.id > _.id))
      Ok(msgs)
  }

  def latestMessageId = SecuredAction {
    request => Ok(Json.toJson(ChatActions.latestId))
  }

  def chatComponentHtml() = SecuredAction {
    request =>
      Ok(views.html.chatlog.render())
  }
}

/**
  * Companion Object, handles chat related database operations
  */
object ChatActions {

  def insertMsg(adminName: String,
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

  /** returns the id of the latest chat message */
  def latestId : Int = DB readOnly { implicit session =>
     sql"SELECT MAX(id) FROM chatlog".map(rs => rs.int(1)).single().apply().getOrElse(0)
  }


  /** Main chat query method, allows to query all combinations of the defined parameters */
  def queryChat(count: Int, page: Int, radio: Boolean, userId: Option[Int], fromMessageId: Option[Int], queryString: Option[String]): Seq[Message] = DB readOnly {
    implicit session =>

      val countryJoin =  sqls"LEFT OUTER JOIN countries ON chatlog.client_id = countries.id"

      val offset = if (fromMessageId.isDefined) {
        fromMessageId.get - count * page
      } else {
        count * page
      }
      val radioLike =
        if (!radio)
          sqls.notLike(sqls"msg", "RADIO" + "%")
        else
          sqls.like(sqls"msg", "%")

      val userLike =
        if (userId.isDefined)
          sqls.eq(sqls"client_id", userId.get)
        else
          sqls.gt(sqls"client_id", -1)


      fromMessageId match {
        case None =>
          queryString match {
            case Some(q) =>
              val likeQuery = sqls.like(sqls"msg", "%" + q + "%")
              sql"""SELECT * FROM chatlog $countryJoin WHERE ${likeQuery} AND ${radioLike} AND ${userLike} ORDER BY chatlog.id DESC LIMIT $count OFFSET $offset""".map(rs => Message.fromRS(rs))
                .list().apply().toSeq
            case _ =>
              sql"""SELECT * FROM chatlog $countryJoin WHERE ${radioLike} AND ${userLike} ORDER BY chatlog.id DESC  LIMIT $count OFFSET $offset""".map(rs => Message.fromRS(rs)).list().apply().toSeq
          }

        case Some(id) =>
          val offset2 = count * page + sql"""SELECT COUNT(*)  FROM chatlog WHERE chatlog.id > ${id} AND ${radioLike} AND ${userLike}""".map(rs => rs.int(1)).single().apply().getOrElse(0)
          queryString match {
            case Some(q) =>
              val likeQuery = sqls.like(sqls"`msg`", "%" + q + "%")
              sql"""SELECT * FROM chatlog $countryJoin WHERE ${likeQuery} AND ${radioLike}  AND ${userLike} ORDER BY chatlog.id DESC LIMIT $count OFFSET $offset2""".map(rs => Message.fromRS(rs))
                .list().apply().toSeq
            case _ =>
              println("Fo bar baz")
              sql"""SELECT * FROM chatlog $countryJoin WHERE ${radioLike} AND ${userLike} ORDER BY chatlog.id DESC LIMIT $count OFFSET $offset2""".map(rs => Message.fromRS(rs)).list().apply().toSeq

          }
      }
  }
}