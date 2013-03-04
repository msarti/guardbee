package com.elogiclab.oauth2.authz.core

import org.joda.time.DateTime
import play.api.Logger
import play.api.Application
import play.api.Plugin

trait AuthorizationRequest {
  def code: String
  
  def client_id: String
  def user: String
  def response_type: String
  def redirect_uri: String
  def scope: String
  def state: Option[String]
  def request_timestamp: DateTime
  def request_expiration: DateTime
  
  def isExpired = DateTime.now isAfter request_expiration
}


case class SimpleAutorizationRequest(
    code: String, 
    client_id: String, 
    user: String,
    response_type: String,
    redirect_uri: String,
  	scope: String,
  	state: Option[String],
  	request_timestamp: DateTime,
  	request_expiration: DateTime
) extends AuthorizationRequest

trait UserAuthorization {
  def client_id: String
  def user: String
  def granted_on: DateTime
}

case class SimpleUserAuthorization(client_id: String, user: String, granted_on: DateTime) extends UserAuthorization

trait UserAuthorizationService {

  def save(authorization: UserAuthorization): UserAuthorization
  
  def saveRequest(authzRequest: AuthorizationRequest): AuthorizationRequest
  
  def consumeRequest(requestCode: String): Option[AuthorizationRequest]

  def findByClientIdAndUser(clientId: String, userId: String): Option[UserAuthorization]

  def delete(clientId: String, userId: String): Unit

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

  def saveRequest(authzRequest: AuthorizationRequest): AuthorizationRequest = {
    delegate.map(_.saveRequest(authzRequest)).getOrElse {
      notInitialized()
      authzRequest
    }
  }
  
  def consumeRequest(requestCode: String): Option[AuthorizationRequest] = {
    delegate.map(_.consumeRequest(requestCode)).getOrElse {
      notInitialized()
      None
    }
  }
  
  def delete(clientId: String, userId: String): Unit = {
    delegate.map(_.delete(clientId, userId)).getOrElse {
      notInitialized()
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