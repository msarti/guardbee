package com.elogiclab.guardbee.model

import com.elogiclab.guardbee.core._
import com.elogiclab.guardbee.model._
import org.joda.time.DateTime
import play.api.db._
import anorm._
import anorm.SqlParser._
import java.util.Date
import org.joda.time.DateTimeZone
import play.api.Play.current

case class SimpleAccessToken(token: String,
  refresh_token: String,
  token_type: String,
  user: String,
  client_id: String,
  scope: Seq[Scope],
  issued_on: DateTime,
  token_expiration: DateTime,
  refresh_token_expiration: DateTime) extends AccessToken

object SimpleAccessToken {

  val simple = {
    get[String]("access_token.token") ~
      get[String]("access_token.refresh_token") ~
      get[String]("access_token.token_type") ~
      get[String]("access_token.user") ~
      get[String]("access_token.client_id") ~
      get[Date]("access_token.issued_on") ~
      get[Date]("access_token.token_expiration") ~
      get[Date]("access_token.refresh_token_expiration") map {
        case token ~ refresh_token ~ token_type ~ user ~ client_id ~ issued_on ~ token_expiration ~ refresh_token_expiration =>
          SimpleAccessToken(token, refresh_token, token_type, user, client_id, getScope(token), new DateTime(issued_on), new DateTime(token_expiration), new DateTime(refresh_token_expiration))
      }

  }

  def create(token: AccessToken): AccessToken = {
    DB.withTransaction { implicit connection =>

      SQL(
        """
           insert into access_token (
           token, refresh_token, token_type, user, client_id, issued_on, token_expiration, refresh_token_expiration
           ) values (
           {token}, {refresh_token}, {token_type}, {user}, {client_id}, {issued_on}, {token_expiration}, {refresh_token_expiration}
           )
         """).on(
          'token -> token.token,
          'refresh_token -> token.refresh_token,
          'token_type -> token.token_type,
          'user -> token.user,
          'client_id -> token.client_id,
          'issued_on -> token.issued_on.toDate,
          'token_expiration -> token.token_expiration.toDate,
          'refresh_token_expiration -> token.refresh_token_expiration.toDate).executeUpdate()

      token.scope.foreach {
        scope =>
          SQL("insert into token_scope (token, scope) values ({token}, {scope})").on('token -> token.token, 'scope -> scope.scope)
      }

      SimpleAccessToken(token.token, token.refresh_token, token.token_type, token.user, token.client_id, token.scope, token.issued_on, token.token_expiration, token.refresh_token_expiration)
    }
  }

  def delete(token: String): Unit = {
    DB.withConnection { implicit connection =>
      SQL(
      """
          delete from access_token
          where token = {token}
      """
      ).on('token -> token).executeUpdate()
    }
  }
  def findByToken(token: String): Option[AccessToken] = {
    DB.withConnection { implicit connection =>
      SQL(
      """
          select access_token.* from access_token
          where access_token.token = {token}
      """
      ).on('token -> token).as(SimpleAccessToken.simple.singleOpt)
    }
  }
  
  def getScope(token: String ):Seq[Scope] = {
    DB.withConnection { implicit connection =>
      SQL(
      """
          select scope.* from scope
          join token_scope on token_scope.scope = scope.scope
          where token_scope.token = {token}
      """
      ).on('token -> token).as(SimpleScope.simple *)
      
    }
  }

}
