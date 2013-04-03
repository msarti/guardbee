package com.elogiclab.guardbee.model

import com.elogiclab.guardbee.core.Scope
import play.api.db._
import anorm._
import anorm.SqlParser._
import play.api.Play.current

case class SimpleScope(scope: String, description: String) extends Scope


object SimpleScope {

  val simple = {
    get[String]("scope.scope") ~
      get[String]("scope.description") map {
        case scope ~ description =>
          SimpleScope(scope, description)
      }
  }

  def create(scope: Scope): Scope = {
    DB.withTransaction { implicit connection =>

      SQL(
        """
           insert into scope (
           scope, description
           ) values (
           {scope}, {description}
           )
         """).on(
          'scope -> scope.scope,
          'description -> scope.description).executeUpdate()

      SimpleScope(scope.scope, scope.description)
    }
  }

  def findByCode(scope: String): Option[Scope] = {
    DB.withConnection { implicit connection =>
      SQL("select * from scope where scope = {scope}").on(
        'scope -> scope).as(SimpleScope.simple.singleOpt)
    }
  }

  def delete(scope: String): Unit = {
    DB.withConnection { implicit connection =>
      SQL("delete from scope where scope = {scope}").on(
        'scope -> scope).executeUpdate()
    }
  }

}