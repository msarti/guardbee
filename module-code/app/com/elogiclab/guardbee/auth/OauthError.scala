package com.elogiclab.guardbee.auth

import play.api.http.Status._
import play.api.i18n.Messages

case class OauthError(status_code:Int = BAD_REQUEST, error: String = "invalid_request", description: String)

object OauthError {
  
  val EXPIRED_REFRESH_TOKEN = OauthError(BAD_REQUEST, "invalid_request", Messages("guardbee.error.expired_refresh_token"))
  val INVALID_REFRESH_TOKEN = OauthError(BAD_REQUEST, "invalid_request", Messages("guardbee.error.invalid_refresh_token"))
  val INVALID_TOKEN = OauthError(BAD_REQUEST, "invalid_request", Messages("guardbee.error.invalid_token"))
                                    
  
  
}