package com.elogiclab.guardbee.auth

import play.api.http.Status._
import play.api.i18n.Messages
import play.api.mvc._

case class OauthError(status_code: Int = BAD_REQUEST, error: String = "invalid_request", description: String)

object OauthError 
{
  val EXPIRED_REFRESH_TOKEN = OauthError(BAD_REQUEST, "invalid_request", Messages("guardbee.error.expired_refresh_token"))
  val INVALID_REFRESH_TOKEN = OauthError(BAD_REQUEST, "invalid_request", Messages("guardbee.error.invalid_refresh_token"))
  val INVALID_TOKEN = OauthError(BAD_REQUEST, "invalid_request", Messages("guardbee.error.invalid_token"))
  val INVALID_AUTHENTICATION_HEADER = OauthError(BAD_REQUEST, "invalid_request", Messages("guardbee.error.invalid_authentication_header"))
  val AUTHENTICATION_REQUIRED = OauthError(FORBIDDEN, "forbidden", Messages("guardbee.error.authentication_required"))
  val UNAUTHORIZED_ACCESS = OauthError(UNAUTHORIZED, "unauthorized", Messages("guardbee.error.unauthorized"))

}