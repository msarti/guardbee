package com.elogiclab.oauth2.authz.core

import java.util.Date
import play.api.Logger
import play.api.Application
import play.api.Plugin
import org.joda.time.DateTime

trait AuthCode {
  def code: String
  def userId: String
  def creationTime: DateTime
  def expireOn: DateTime
}

case class SimpleAuthCode(
  code: String,
  userId: String,
  creationTime: DateTime,
  expireOn: DateTime) extends AuthCode

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

  private def notInitialized() {
    Logger.error("AuthCodeService was not initialized. Make sure a AuthCodeService plugin is specified in your play.plugins file")
    throw new RuntimeException("AuthCodeService not initialized")
  }

}