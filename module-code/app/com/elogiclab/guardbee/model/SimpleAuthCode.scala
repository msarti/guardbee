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

case class SimpleAuthCode(
  code: String,
  user: String,
  redirect_uri: String,
  scope: Seq[Scope],
  issued_on: DateTime,
  expire_on: DateTime) extends AuthCode

object SimpleAuthCode {

  val simple = {
    get[String]("auth_code.code") ~
      get[String]("auth_code.user") ~
      get[String]("auth_code.redirect_uri") ~
      get[Date]("auth_code.issued_on") ~
      get[Date]("auth_code.expire_on") map {
        case code ~ user ~ redirect_uri ~ issued_on ~ expire_on =>
          SimpleAuthCode(code, user, redirect_uri, getScope(code), new DateTime(issued_on), new DateTime(expire_on))
      }
  }

  
  def create(code: AuthCode): AuthCode = {
    DB.withTransaction { implicit connection =>
      SQL(
        """
           insert into auth_code (
           code, user, redirect_uri, issued_on, expire_on
           ) values (
           {code}, {user}, {redirect_uri}, {issued_on}, {expire_on}
           )
         """).on(
          'code -> code.code,
          'user -> code.user,
          'redirect_uri -> code.redirect_uri,
          'issued_on -> code.issued_on.toDate,
          'expire_on -> code.expire_on.toDate).executeUpdate()

      code.scope.foreach {
        scope =>
          SQL("insert into auth_code_scope (code, scope) values ({code}, {scope})").on('code -> code.code, 'scope -> scope.scope)
      }
      code
    }
  }
  
  
  def consume(code: String):Option[AuthCode] = {
    DB.withTransaction { implicit connection =>
      SQL(
        """
          select auth_code.* from auth_code
          where auth_code.code = {code}
      """).on('code -> code).as(SimpleAuthCode.simple.singleOpt) match {
          case Some(authCode) => {
            SQL("delete from auth_code where code = {code}").on('code -> code).executeUpdate()
            Some(authCode)
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
          join auth_code_scope on auth_code_scope.scope = scope.scope
          where auth_code_scope.code = {code}
      """).on('code -> code).as(SimpleScope.simple *)

    }
  }
  
}