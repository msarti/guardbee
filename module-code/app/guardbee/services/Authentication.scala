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

import java.util.UUID

import org.joda.time.DateTime

import play.api.Play
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms.boolean
import play.api.data.Forms.optional
import play.api.data.Forms.single
import play.api.libs.Crypto
import play.api.mvc.AnyContent
import play.api.mvc.Cookie
import play.api.mvc.Request

case class Authentication(username: String, user: User, grants: Seq[String], provider: String, scope: Option[Seq[String]], lastAccess: DateTime = DateTime.now) {

  def isExpired = lastAccess.plusMinutes(Authentication.idleTimeout).isBefore(DateTime.now)

  def touch = this.copy(lastAccess = DateTime.now)
}

object Authentication {
  val Root = "/"

  val cookieNameKey = "guardbee.cookie.name"
  val cookiePathKey = "guardbee.cookie.path"
  val cookieSecureKey = "guardbee.cookie.secure"
  val timeoutKey = "guardbee.cookie.timeoutInMinutes"
  val idleTimeoutKey = "guardbee.idleTimeoutInMinutes"
  val afterLoginUrlKey = "guardbee.afterLoginUrl"
  val afterLogoutUrlKey = "guardbee.afterLogoutUrl"

  val defaultCookieName = "guardbee.id"
  val defaultCookiePath = Root
  val defaultAbsoluteTimeout = 10080 //7 days
  val defaultIdleTimeout = 60
  val applicationContext = "application.context"

  lazy val cookieName = Play.application.configuration.getString(cookieNameKey).getOrElse(defaultCookieName)
  lazy val cookiePath = Play.application.configuration.getString(cookiePathKey).getOrElse(
    Play.configuration.getString(applicationContext).getOrElse(defaultCookiePath))
  lazy val cookieTimeout = Play.application.configuration.getInt(timeoutKey).getOrElse(defaultAbsoluteTimeout)
  lazy val idleTimeout = Play.application.configuration.getInt(idleTimeoutKey).getOrElse(defaultIdleTimeout)
  lazy val cookieSecure = Play.application.configuration.getBoolean(cookieSecureKey).getOrElse(false)

  lazy val afterLogoutUrl = Play.application.configuration.getString(afterLogoutUrlKey).getOrElse(Root)

  def createCookie(implicit request: Request[AnyContent]) = {
    val maxAge = Form(single("remember-me" -> optional(boolean))).bindFromRequest.get.getOrElse(false) match {
      case true => Some(cookieTimeout)
      case false => None
    }
    val value = UUID.randomUUID.toString
    (Cookie(
      name = cookieName,
      path = cookiePath,
      maxAge = maxAge,
      value = Crypto.sign(value)+"-"+value,
      httpOnly = true,
      secure = cookieSecure), value)
  }

  lazy val expireCookie = Cookie(
    name = cookieName,
    maxAge = Some(-1),
    value = "")
  
  def checkSign(signed:String): Option[String] = {
    val Array(sign, value) = signed.split("-", 2)
    if(Crypto.sign(value) == sign) {
      Some(value)
    } else {
      None
    }
  }
    
  def getCookie(implicit request: Request[AnyContent]): Option[String] = {
    request.cookies.get(cookieName).flatMap {
      cookie => 
        (for(v <- Some(cookie.value) if cookie.value.contains("-")) yield v) match {
          case Some(signed) => checkSign(signed)
          case None => None
        }
    }
  }

}