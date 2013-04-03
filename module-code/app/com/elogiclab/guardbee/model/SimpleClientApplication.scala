package com.elogiclab.guardbee.model

import com.elogiclab.guardbee.core.ClientApplication
import org.joda.time.DateTime
import play.api.db._
import anorm._
import anorm.SqlParser._
import java.util.Date
import org.joda.time.DateTimeZone
import play.api.Play.current

case class SimpleClientApplication(
  client_id: String,
  client_secret: String,
  owner_user: String,
  app_name: String,
  app_description: Option[String],
  redirect_uris: Seq[String],
  issued_on: DateTime) extends ClientApplication

object SimpleClientApplication {

  val simple = {
    get[String]("application.client_id") ~
      get[String]("application.client_secret") ~
      get[String]("application.owner_user") ~
      get[String]("application.app_name") ~
      get[Option[String]]("application.app_description") ~
      get[String]("application.redirect_uris") ~
      get[Date]("application.issued_on") map {
        case client_id ~ client_secret ~ owner_user ~ app_name ~ app_description ~ redirect_uris ~ issued_on =>
          SimpleClientApplication(client_id, client_secret, owner_user, app_name, app_description, redirect_uris.split(","), new DateTime(issued_on))
      }

  }

  def create(app: ClientApplication) = {
    DB.withTransaction { implicit connection =>

      SQL(
        """
           insert into application (
           client_id, client_secret, owner_user, app_name, app_description, redirect_uris, issued_on
           ) values (
           {client_id}, {client_secret}, {owner_user}, {app_name}, {app_description}, {redirect_uris}, {issued_on}
           )
         """).on(
          'client_id -> app.client_id,
          'client_secret -> app.client_secret,
          'owner_user -> app.owner_user,
          'app_name -> app.app_name,
          'app_description -> app.app_description,
          'redirect_uris -> app.redirect_uris.mkString(","),
          'issued_on -> app.issued_on.toDate).executeUpdate()

      SimpleClientApplication(app.client_id, app.client_secret, app.owner_user, app.app_name, app.app_description, app.redirect_uris, app.issued_on)
    }
  }

  def delete(client_id: String): Unit = {
    DB.withConnection { implicit connection =>
      SQL("delete from application where clientId = {clientId}").on(
        'client_id -> client_id).executeUpdate()
    }
  }

  def findByClientId(client_id: String): Option[ClientApplication] = {
    DB.withConnection { implicit connection =>
      SQL("select * from application where client_id = {client_id}").on(
        'client_id -> client_id
      ).as(SimpleClientApplication.simple.singleOpt)  
    }
  }
}