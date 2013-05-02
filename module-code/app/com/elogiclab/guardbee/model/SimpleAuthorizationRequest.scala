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
import play.api.Logger

case class SimpleAutorizationRequest(
  code: String,
  client_id: String,
  user: String,
  response_type: String,
  redirect_uri: String,
  scope: Seq[Scope],
  state: Option[String],
  request_timestamp: DateTime,
  request_expiration: DateTime) extends AuthorizationRequest

object SimpleAutorizationRequest {

  val simple = {
    get[String]("authorization_request.code") ~
      get[String]("authorization_request.client_id") ~
      get[String]("authorization_request.user") ~
      get[String]("authorization_request.response_type") ~
      get[String]("authorization_request.redirect_uri") ~
      get[Option[String]]("authorization_request.state") ~
      get[Date]("authorization_request.request_timestamp") ~
      get[Date]("authorization_request.request_expiration") map {
        case code ~ client_id ~ user ~ response_type ~ redirect_uri ~ state ~ request_timestamp ~ request_expiration =>
          SimpleAutorizationRequest(code, client_id, user, response_type, redirect_uri, getScope(code), state, new DateTime(request_timestamp), new DateTime(request_expiration))
      }
  }

  def create(req: AuthorizationRequest): AuthorizationRequest = {
    DB.withTransaction { implicit connection =>
      Logger.debug("Creating new authorization request: "+req)
      SQL(
        """
           insert into authorization_request (
           code, client_id, user, response_type, redirect_uri, state, request_timestamp, request_expiration
           ) values (
           {code}, {client_id}, {user}, {response_type}, {redirect_uri}, {state}, {request_timestamp}, {request_expiration}
           )
         """).on(
          'code -> req.code,
          'client_id -> req.client_id,
          'user -> req.user,
          'response_type -> req.response_type,
          'redirect_uri -> req.redirect_uri,
          'state -> req.state,
          'request_timestamp -> req.request_timestamp.toDate,
          'request_expiration -> req.request_expiration.toDate).executeUpdate()

      req.scope.foreach {
        scope =>
          SQL("insert into auth_request_scope (code, scope) values ({code}, {scope})").on('code -> req.code, 'scope -> scope.scope).executeUpdate
      }
      req
    }

  }

  def consume(code: String): Option[AuthorizationRequest] = {
    DB.withTransaction { implicit connection =>
      SQL(
        """
          select authorization_request.* from authorization_request
          where authorization_request.code = {code}
      """).on('code -> code).as(SimpleAutorizationRequest.simple.singleOpt) match {
          case Some(req) => {
            val result = req.copy(scope = getScope(code))
            
            Some(req.copy(scope = getScope(code)))
          }
          case None => None
        }
    }
  }

  def getScope(code: String): Seq[Scope] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select scope.* from scope
          join auth_request_scope on auth_request_scope.scope = scope.scope
          where auth_request_scope.code = {code}
      """).on('code -> code).as(SimpleScope.simple *)

    }
  }
}