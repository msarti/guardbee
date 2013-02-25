package com.elogiclab.oauth2.authz.core

import org.joda.time.DateTime
import play.api.Logger
import play.api.Application
import play.api.Plugin

trait UserAuthorization {
  def clientId: String
  def userId: String
  def grantedOn: DateTime
  def enabled: Boolean
}

case class SimpleUserAuthorization(clientId: String, userId: String, grantedOn: DateTime, enabled: Boolean) extends UserAuthorization

trait UserAuthorizationService {

  def save(authorization: UserAuthorization): UserAuthorization

  def findByClientIdAndUser(clientId: String, userId: String): Option[UserAuthorization]

  def delete(clientId: String, userId: String): Unit

  def enable(clientId: String, userId: String): Option[UserAuthorization]

}

abstract class UserAuthorizationServicePlugin(application: Application) extends Plugin with UserAuthorizationService {
  override def onStart() {
    UserAuthorizationService.setService(this)
  }
}




object UserAuthorizationService {

  var delegate: Option[UserAuthorizationService] = None

  def setService(service: UserAuthorizationService) = {
    delegate = Some(service);
  }

  def save(authorization: UserAuthorization): UserAuthorization = {
    delegate.map(_.save(authorization)).getOrElse {
      notInitialized()
      authorization
    }
  }

  def delete(clientId: String, userId: String): Unit = {
    delegate.map(_.delete(clientId, userId)).getOrElse {
      notInitialized()
    }
  }

  def enable(clientId: String, userId: String): Option[UserAuthorization] = {
    delegate.map(_.enable(clientId, userId)).getOrElse {
      notInitialized()
      None
    }
  }

  def findByClientIdAndUser(clientId: String, userId: String): Option[UserAuthorization] = {
    delegate.map(_.findByClientIdAndUser(clientId, userId)).getOrElse {
      notInitialized()
      None
    }
  }
  private def notInitialized() {
    Logger.error("UserAuthorizationService was not initialized. Make sure a UserAuthorizationService plugin is specified in your play.plugins file")
    throw new RuntimeException("UserAuthorizationService not initialized")
  }

}