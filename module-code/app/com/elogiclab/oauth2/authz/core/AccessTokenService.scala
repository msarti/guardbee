package com.elogiclab.oauth2.authz.core

import java.util.Date
import play.api.Logger
import play.api.Application
import play.api.Plugin

trait AccessToken {
  def token: String
  def tokenType: String
  def userId: String
  def scope: Seq[Scope]
  def issuedOn: Date
  def expiresOn: Date
}


trait AccessTokenService {
  def save(token: AccessToken): AccessToken
  def delete(token: String): Unit
  def findByToken(token: String): Option[AccessToken]
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

  def delete(token: String): Unit = {
    delegate.map(_.delete(token)).getOrElse {
      notInitialized()
      None
    }
  }

  private def notInitialized() {
    Logger.error("AuthCodeService was not initialized. Make sure a AuthCodeService plugin is specified in your play.plugins file")
    throw new RuntimeException("AuthCodeService not initialized")
  }

}