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


package guardbee.services.providers

import play.api.Application
import guardbee.services.PasswordProvider
import scala.util.Right
import play.api.i18n.Messages
import play.api.data.Form
import play.api.mvc.Request
import play.api.mvc.AnyContent
import play.api.data._
import play.api.data.Forms._
import play.api.Logger
import guardbee.services.Password
import guardbee.services.User
import guardbee.services.Credentials
import play.api.mvc.Result
import play.api.mvc.Results
import guardbee.services.Authentication
import org.jboss.netty.handler.codec.http.Cookie
import guardbee.services.AuthStoreProvider
import play.api.mvc.RequestHeader
import play.api.Play
import play.api.libs.json.Json
import guardbee.utils.RoutesHelper
import guardbee.services.UserService
import guardbee.utils.GuardbeeError
import guardbee.services.PersistentAuthenticationProvider
case class PlainPassword(password: String, rememberMe: Boolean) extends Credentials

class UsernamePasswordAuthenticationProvider(application: Application)
  extends PersistentAuthenticationProvider(application)  {

  type CredentialsType = PlainPassword

  val id = "usernamepassword"
  val unique = true;

  val form = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "remember-me" -> optional(boolean))((username, password, rememberMe) => AuthenticationToken(name = username, credentials = Some(PlainPassword(password, rememberMe.getOrElse(false)))))((auth: AuthenticationToken) => Some((auth.name, auth.credentials.map(_.password).getOrElse(""), Some(false)))))

  def extractAuthToken(implicit request: Request[AnyContent]): Either[GuardbeeError, AuthenticationToken] = {
    form.bindFromRequest.fold(
      formWithErrors => {
        Logger.warn(formWithErrors.errors.map(f => f.key + "->" + f.message).mkString)
        Left(GuardbeeError.AuthenticationError)
      },
      success => {
        Logger.debug("Authentication attempted: username = " + success.name + ", credentials = <hidden>, remember-me = " + success.credentials.map(_.rememberMe).getOrElse(false))

        Right(success)
      })
  }


  
  
  override def authenticate(authToken: AuthenticationToken)(implicit request: Request[AnyContent]): Either[GuardbeeError, Authentication] = {
    val principal = for (
      user <- UserService.getUserByID(authToken.name) if user.enabled;
      pwd <- UserService.getUserPassword(user);
      candidate <- authToken.credentials;
      hasher <- PasswordProvider.getDelegate(pwd.hasher) if hasher.matches(candidate.password, pwd)
    ) yield user
    principal match {
      case None => {
        
        Logger.warn("User "+authToken.name+": invalid credentials")
        Left(GuardbeeError.AuthenticationError)
      }
      case Some(value) => {
        Logger.info("User "+value.user_id+" succesfully authenticated")
        Right(Authentication(value.user_id, value, UserService.getUserGrants(value), id, None))
      }
    }
  }


  def afterLoginUrl(implicit request: Request[AnyContent]) = {
    import Play.current
    request.cookies.get("original-url").map(_.value).getOrElse(
      Play.configuration.getString(Authentication.afterLoginUrlKey).getOrElse(
        Play.configuration.getString(Authentication.applicationContext).getOrElse("/")))
  }



  def isAjax(implicit request: Request[AnyContent]): Boolean = {
    request.headers.get("X-Requested-With").getOrElse("").equals("XMLHttpRequest")
  }

  def onLoginSuccess(authentication: Authentication)(implicit request: Request[AnyContent]): Result = {

    def ajax: Result = {
      Results.Ok(Json.obj("status" -> "OK", "toUrl" -> afterLoginUrl))
    }
    if (isAjax)
      ajax
    else
      Results.Redirect(afterLoginUrl)
  }
  def onLoginFailure(implicit request: Request[AnyContent]): Result = {
    def ajax: Result = {
      Results.Unauthorized(Json.obj("status" -> "ERROR", "error" -> "invalid username/password"))
    }
    if (isAjax)
      ajax
    else
      Results.Redirect(RoutesHelper.loginPage(afterLoginUrl)).flashing( ("error", "invalid username/password") )
    
  }

}