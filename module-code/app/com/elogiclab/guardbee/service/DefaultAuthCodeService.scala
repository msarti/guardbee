package com.elogiclab.guardbee.service

import com.elogiclab.guardbee.core.AuthCodeServicePlugin
import play.api.Application
import com.elogiclab.guardbee.core.AuthCode
import com.elogiclab.guardbee.model.SimpleAuthCode

class DefaultAuthCodeService(application: Application) extends AuthCodeServicePlugin(application) {
  def save(code: AuthCode): AuthCode = SimpleAuthCode.create(code)

  /**
   * @param code 
   * @return
   */
  def consume(code: String): Option[AuthCode] = SimpleAuthCode.consume(code)
}