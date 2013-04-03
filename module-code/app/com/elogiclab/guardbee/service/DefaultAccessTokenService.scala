package com.elogiclab.guardbee.service

import com.elogiclab.guardbee.core.AccessTokenServicePlugin
import play.api.Application
import com.elogiclab.guardbee.core.AccessToken
import com.elogiclab.guardbee.model._


class DefaultAccessTokenService(application: Application) extends AccessTokenServicePlugin(application) {
  def save(token: AccessToken): AccessToken = SimpleAccessToken.create(token)

  def delete(token: String): Unit = SimpleAccessToken.delete(token)

  def findByToken(token: String): Option[AccessToken] = SimpleAccessToken.findByToken(token)

}