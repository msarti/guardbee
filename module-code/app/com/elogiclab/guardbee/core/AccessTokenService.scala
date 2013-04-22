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

import play.api.Logger
import play.api.Application
import play.api.Plugin
import org.joda.time.DateTime
import org.joda.time.Seconds
import play.api.mvc.AnyContent
import java.util.UUID
import com.elogiclab.guardbee.model._
import com.elogiclab.guardbee.auth._
import com.elogiclab.guardbee.auth.OauthError._

trait AccessToken {
  def token: String
  def refresh_token: String
  def token_type: String
  def user: String
  def client_id: String
  def scope: Seq[Scope]
  def issued_on: DateTime
  def token_expiration: DateTime
  def refresh_token_expiration: DateTime

  def expires_in = Seconds.secondsBetween(issued_on, token_expiration)

  def isTokenExpired = DateTime.now.isAfter(token_expiration)
  def isRefreshTokenExpired = DateTime.now.isAfter(refresh_token_expiration)
  
 // def asJson = Map("access_token" -> token, 
 //     "token_type" -> token_type, 
 //     "expires_in" -> Seconds.secondsBetween(DateTime.now, token_expiration),
 //     "refresh_token" -> refresh_token)
}


trait AccessTokenService {
  def save(token: AccessToken): AccessToken
  def delete(token: String): Unit
  def findByToken(token: String): Option[AccessToken]
  def findByRefreshToken(refresh_token: String): Option[AccessToken]

}

abstract class AccessTokenServicePlugin(application: Application) extends Plugin with AccessTokenService {

  override def onStart() {
    AccessTokenService.setService(this)
  }

}

object AccessTokenService {
  var delegate: Option[AccessTokenService] = None

  def setService(service: AccessTokenService) = {
    delegate = Some(service);
  }

  def save(token: AccessToken): AccessToken = {
    delegate.map(_.save(token)).getOrElse {
      notInitialized()
      token
    }
  }
  def findByToken(token: String): Option[AccessToken] = {
    delegate.map(_.findByToken(token)).getOrElse {
      notInitialized()
      None
    }
  }
  def findByRefreshToken(refresh_token: String, validate: Boolean = true): Either[AccessToken, OauthError] = {
    delegate.map(_.findByRefreshToken(refresh_token)).getOrElse {
      notInitialized()
      None
    } match {
      case None => Right(INVALID_REFRESH_TOKEN)
      case Some(token) => if(validate && token.isRefreshTokenExpired) Right(EXPIRED_REFRESH_TOKEN) else Left(token)
    } 
  }
  
  def refreshToken(refresh_token: String):Either[AccessToken, OauthError] = {
    findByRefreshToken(refresh_token).fold(
        token => {
          delete(token.token) 
          val new_token = issueToken(token.user, token.client_id, token.scope)
          Left(new_token)
        },
        error => Right(error) )
  }

  def delete(token: String): Unit = {
    delegate.map(_.delete(token)).getOrElse {
      notInitialized()
      None
    }
  }

  def issueToken(user:String, client_id: String, scope: Seq[Scope]) = {
    import play.api.Play
    save(
      SimpleAccessToken(token = UUID.randomUUID().toString(),
        refresh_token = UUID.randomUUID().toString(),
        token_type = "Bearer",
        user = user,
        client_id = client_id,
        scope = scope,
        issued_on = DateTime.now,
        token_expiration = DateTime.now.plusSeconds(Play.current.configuration.getInt("guardbee.access_token.expire_in").getOrElse(86400)),
        refresh_token_expiration = DateTime.now.plusDays(Play.current.configuration.getInt("guardbee.refresh_token.duration_days").getOrElse(30))))

  }
  
  
  def revokeToken(token: String): Boolean = {
    findByToken(token) match {
      case None => false
      case _ => {
        delete(token)
        true
      }
    }
  }
  
  

  private def notInitialized() {
    Logger.error("AuthCodeService was not initialized. Make sure a AuthCodeService plugin is specified in your play.plugins file")
    throw new RuntimeException("AuthCodeService not initialized")
  }

}