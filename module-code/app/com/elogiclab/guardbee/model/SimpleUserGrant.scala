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

case class SimpleUserGrant(client_id: String,
  user: String,
  scope: Seq[Scope],
  granted_on: DateTime) extends UserGrant

object SimpleUserGrant {

  val simple = {
    get[String]("user_grant.client_id") ~
      get[String]("user_grant.user") ~
      get[Date]("user_grant.granted_on") map {
        case client_id ~ user ~ granted_on =>
          SimpleUserGrant(client_id, user, getScope(client_id, user), new DateTime(granted_on))
      }
  }

  def create(grant: UserGrant): UserGrant = {
    DB.withTransaction { implicit connection =>
      SQL(
        """
           insert into user_grant (
           client_id, user, granted_on
           ) values (
           {client_id}, {user}, {granted_on}
           )
         """).on(
          'client_id -> grant.client_id,
          'user -> grant.user,
          'granted_on -> grant.granted_on.toDate).executeUpdate()

      grant.scope.foreach {
        scope =>
          SQL("insert into user_grant_scope (client_id, user, scope) values ({client_id}, {user}, {scope})").on('client_id -> grant.client_id, 'user -> grant.user, 'scope -> scope.scope)
      }
      grant
    }
  }

  def delete(client_id: String, user: String): Unit = {
    DB.withTransaction { implicit connection =>
      SQL("delete from user_grant_scope where client_id = {client_id} and user = {user}").on('client_id -> client_id, 'user -> user).executeUpdate()
      SQL("delete from user_grant where client_id = {client_id} and user = {user}").on('client_id -> client_id, 'user -> user).executeUpdate()

    }
  }

  def findByClientIdAndUser(client_id: String, user: String): Option[UserGrant] = {
    DB.withConnection { implicit connection =>
      SQL("select * from user_grant where client_id = {client_id} and user = {user}").on('client_id -> client_id, 'user -> user).as(SimpleUserGrant.simple.singleOpt)
    }
  }

  def getScope(client_id: String, user: String): Seq[Scope] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select scope.* from scope
          join user_grant_scope on user_grant_scope.scope = scope.scope
          where user_grant_scope.client_id = {client_id} and user_grant_scope.user = {user}
      """).on('client_id -> client_id, 'user -> user).as(SimpleScope.simple *)

    }
  }

}