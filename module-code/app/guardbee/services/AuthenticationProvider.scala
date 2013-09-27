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

package guardbee.services

import play.api.Application
import play.api.Plugin
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.mvc.AnyContent
import play.api.mvc.Result
import play.api.mvc.Cookie
import play.api.mvc.Results
import java.util.UUID
import play.api.mvc.Cookie
import play.api.Play
import play.api.libs.Crypto
import play.api.Logger
import org.joda.time.DateTime
import guardbee.utils.GuardbeeError

trait Credentials

case class Password(hasher: String, password: String, salt: Option[String] = None)

trait User {
  def user_id: String
  def email: String
  def created_on: DateTime
  def enabled: Boolean
  def full_name: Option[String]
  def avatar_url: Option[String]
  def bio: Option[String]
  def home_page: Option[String] 
}


abstract class AuthenticationProvider(app: Application) extends BasePlugin {

  type CredentialsType <: Credentials

  case class AuthenticationToken(
    name: String,
    credentials: Option[CredentialsType])

  final val unique = false

  def extractAuthToken(implicit request: Request[AnyContent]): Either[GuardbeeError, AuthenticationToken]
  def authenticate(authToken: AuthenticationToken)(implicit request: Request[AnyContent]): Either[GuardbeeError, Authentication]

  def onLoginSuccess(authentication: Authentication)(implicit request: Request[AnyContent]): Result
  def onLoginFailure(implicit request: Request[AnyContent]): Result
  
  
  def getAuthentication(implicit request: Request[AnyContent]): Either[GuardbeeError, Authentication] = {
    
    Authentication.getCookie flatMap {
      cookie =>
        AuthStoreProvider.get(cookie).filter(a => a.isExpired == false)
    } toRight (GuardbeeError.AuthenticationRequiredError)
  }
  
  def isAuthenticated(implicit request: Request[AnyContent]): Boolean = getAuthentication.isRight
  
  def handleAuthentication(implicit request: Request[AnyContent]): Either[GuardbeeError, Authentication] = {
    for (
      token <- extractAuthToken.right;
      auth <- authenticate(token).right
    ) yield auth
  }
  
  def commit(authentication: Authentication, onAuthorized: => Authentication => Result)(implicit request: Request[AnyContent]): Result = {
    val (cookie, value) = Authentication.createCookie
    AuthStoreProvider.save(value, authentication)
    onAuthorized(authentication).withCookies(cookie)
  }

  def handleLogout(implicit request: Request[AnyContent]): Result = {
    
    Authentication.getCookie match {
      case Some(value) => AuthStoreProvider.delete(value)
      case _ => 
    }
    Results.Redirect(Authentication.afterLogoutUrl).withCookies(Authentication.expireCookie)
  }

  //def logout: Unit

  override def onStart() = {
    AuthenticationProvider.setService(this)
  }

  def handleLogin(onAuthorized: => Authentication => Result, onUnauthorized: => Result)(implicit request: Request[AnyContent]): Result = {
    handleAuthentication match {
      case Left(error) => onUnauthorized
      case Right(success) => commit(success, onAuthorized)
    }
  }

  def handleLogin(implicit request: Request[AnyContent]): Result = {
    handleLogin(onLoginSuccess, onLoginFailure)
  }

}

object AuthenticationProvider extends ServiceCompanion[AuthenticationProvider] {

  val serviceName = "authenticationProvider"
  val default = "usernamepassword"
    
  val Unique = false

  def handleLogin(provider: String)(onAuthorized: => Authentication => Result, onUnauthorized: => Result)(implicit request: Request[AnyContent]): Result = {
    getDelegate(provider).map(_.handleLogin(onAuthorized, onUnauthorized)).getOrElse {
      notInitialized()
      Results.BadRequest
    }
  }
  def handleLogin(provider: String)(implicit request: Request[AnyContent]): Result = {
    getDelegate(provider).map(_.handleLogin).getOrElse {
      notInitialized()
      Results.BadRequest
    }
  }
  
  def handleLogout(provider: String)(implicit request: Request[AnyContent]): Result = {
    getDelegate(provider).map(_.handleLogout).getOrElse {
      notInitialized()
      Results.BadRequest
    }
  }
  
  def isAuthenticated(provider: String)(implicit request: Request[AnyContent]): Boolean = {
    getDelegate(provider).map(_.isAuthenticated).getOrElse {
      notInitialized()
      false
    }
  }
  
  def isAuthenticated(implicit request: Request[AnyContent]): Boolean = isAuthenticated(default)

  def getAuthentication(provider: String)(implicit request: Request[AnyContent]): Either[GuardbeeError, Authentication] = {
    getDelegate(provider).map(_.getAuthentication).getOrElse {
      notInitialized()
      Left(GuardbeeError.AuthenticationRequiredError)
    }
  }
  
  def getAuthentication(implicit request: Request[AnyContent]):  Either[GuardbeeError, Authentication] = getAuthentication(default)
  
  
  
  def handleLogout(implicit request: Request[AnyContent]): Result = handleLogout(default)
  
}

