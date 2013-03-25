/**
 * Copyright 2013 Marco Sarti - twitter: @marconesarti
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.elogiclab.guardbee.core

import org.joda.time.DateTime
import play.api.Logger
import play.api.Application
import play.api.Plugin
import play.api.mvc.AnyContent
import java.util.UUID
import com.elogiclab.guardbee.auth.AuthWrappedRequest

trait AuthorizationRequest {
  def code: String
  def client_id: String
  def user: String
  def response_type: String
  def redirect_uri: String
  def scope: Seq[Scope]
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
  scope: Seq[Scope],
  state: Option[String],
  request_timestamp: DateTime,
  request_expiration: DateTime) extends AuthorizationRequest

trait UserAuthorization {
  def client_id: String
  def user: String
  def scope: Seq[Scope]
  def granted_on: DateTime
}

case class SimpleUserAuthorization(
  client_id: String,
  user: String,
  scope: Seq[Scope],
  granted_on: DateTime) extends UserAuthorization

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

  def makeUserAuthRequest(client_id: String, response_type: String, redirect_uri: String, scope: Seq[Scope], state: Option[String])(implicit request: AuthWrappedRequest[AnyContent]) = {
    import play.api.Play
    saveRequest(
      SimpleAutorizationRequest(
        code = UUID.randomUUID.toString,
        client_id = client_id,
        user = request.user.username,
        response_type = response_type,
        redirect_uri = redirect_uri,
        scope = scope,
        state = state,
        request_timestamp = DateTime.now,
        request_expiration = DateTime.now.plusSeconds(Play.current.configuration.getInt("guardbee.authz_request.expire_in").getOrElse(300))))
  }

  def grantAuthorization(client_id: String, scope: Seq[Scope])(implicit request: AuthWrappedRequest[AnyContent]) = {
    save(
      SimpleUserAuthorization(
        client_id = client_id,
        user = request.user.username,
        scope = scope,
        granted_on = DateTime.now))
  }

  private def notInitialized() {
    Logger.error("UserAuthorizationService was not initialized. Make sure a UserAuthorizationService plugin is specified in your play.plugins file")
    throw new RuntimeException("UserAuthorizationService not initialized")
  }

}