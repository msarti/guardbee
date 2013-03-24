package com.elogiclab.guardbee.demo

import com.elogiclab.guardbee.core._
import play.api.Application

class InMemoryAccessTokenService(application: Application) extends AccessTokenServicePlugin(application) {
  
  private var tokens = Map[String, AccessToken]()


  def save(token: AccessToken): AccessToken = { 
      tokens += (token.token -> token)
      token 
  }

  def delete(token: String): Unit = tokens -= token

  def findByToken(token: String): Option[AccessToken] = tokens.get(token)


}