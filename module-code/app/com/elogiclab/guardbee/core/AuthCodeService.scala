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

import java.util.Date
import play.api.Logger
import play.api.Application
import play.api.Plugin
import org.joda.time.DateTime
import play.api.mvc.AnyContent
import java.util.UUID
import com.elogiclab.guardbee.auth.AuthWrappedRequest

trait AuthCode {
  def code: String
  def user: String
  def redirect_uri: String
  def scope: Seq[Scope]
  def issued_on: DateTime
  def expire_on: DateTime

  def isExpired = DateTime.now.isAfter(expire_on)
}

case class SimpleAuthCode(
  code: String,
  user: String,
  redirect_uri: String,
  scope: Seq[Scope],
  issued_on: DateTime,
  expire_on: DateTime) extends AuthCode

/**
 * @author marco
 *
 */
trait AuthCodeService {

  /**
   * @param authCode the object to save
   * @return the saved object
   */
  def save(authCode: AuthCode): AuthCode

  /**
   * @param code
   * @return
   */
  def consume(code: String): Option[AuthCode]

}

abstract class AuthCodeServicePlugin(application: Application) extends Plugin with AuthCodeService {

  override def onStart() {
    AuthCodeService.setService(this)
  }

}

object AuthCodeService {
  var delegate: Option[AuthCodeService] = None

  def setService(service: AuthCodeService) = {
    delegate = Some(service);
  }

  def save(authCode: AuthCode): AuthCode = {
    delegate.map(_.save(authCode)).getOrElse {
      notInitialized()
      authCode
    }
  }

  def consume(code: String): Option[AuthCode] = {
    delegate.map(_.consume(code)).getOrElse {
      notInitialized()
      None
    }
  }

  def issueCode(redirect_uri: String, scope: Seq[Scope])(implicit request: AuthWrappedRequest[AnyContent]) = {
    import play.api.Play
    save(SimpleAuthCode(
      code = UUID.randomUUID.toString,
      user = request.user.username,
      redirect_uri = redirect_uri,
      scope = scope,
      issued_on = DateTime.now,
      expire_on = DateTime.now.plusSeconds(Play.current.configuration.getInt("guardbee.auth_code.expire_in").getOrElse(300))))
  }

  private def notInitialized() {
    Logger.error("AuthCodeService was not initialized. Make sure a AuthCodeService plugin is specified in your play.plugins file")
    throw new RuntimeException("AuthCodeService not initialized")
  }

}