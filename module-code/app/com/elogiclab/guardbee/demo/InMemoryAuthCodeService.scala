package com.elogiclab.guardbee.demo

import play.api.Application
import com.elogiclab.guardbee.core._

class InMemoryAuthCodeService(application: Application) extends AuthCodeServicePlugin(application) {
  
  private var codes = Map[String, AuthCode]()

  
  def save(authCode: AuthCode): AuthCode = {
    codes += (authCode.code -> authCode)
    authCode
  }

  /**
   * @param code 
   * @return
   */
  def consume(code: String): Option[AuthCode] = {
    codes.get(code) match {
      case None => None
      case Some(authCode) => {
    	codes -= code
        Some(authCode)
      }
      
    }
  }

}
