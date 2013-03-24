package com.elogiclab.guardbee.controller

import play.api.mvc._
import play.api.libs.json.Json._
import com.elogiclab.guardbee.core._
import com.elogiclab.guardbee.format.Formats._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import org.joda.time.Seconds
import org.joda.time.DateTime
import play.api.Logger

case class RequestTokenForm(code: AuthCode, client_id: ClientApplication, client_secret: String, redirect_uri: String, grant_type: String)

trait TokenEndpoint {
  this: Controller =>

  val requestTokenForm = Form(
    mapping(
      "code" -> of[AuthCode],
      "client_id" -> of[ClientApplication],
      "client_secret" -> text,
      "redirect_uri" -> text,
      "grant_type" -> text.verifying("guardbee.error.invalid_grant_type", v => v == "authorization_code"))(RequestTokenForm.apply)(RequestTokenForm.unapply)
      .verifying("guardbee.error.invalid_secret", x => x.client_id.client_secret == x.client_secret)
      .verifying("guardbee.error.invalid_redirect_uri", x => x.redirect_uri == x.code.redirect_uri))

  implicit def tokenImplicits(s: AccessToken) = new {
    def toResponseMap = Map("access_token" -> toJson(s.token), 
      "token_type" -> toJson(s.token_type), 
      "expires_in" -> toJson(Seconds.secondsBetween(DateTime.now, s.token_expiration).getSeconds),
      "refresh_token" -> toJson(s.refresh_token))
  }

  def token = Action { implicit request =>
    requestTokenForm.bindFromRequest()(request).fold(
      hasErrors => {
        for(err <- hasErrors.errors) {
          Logger.info(err.message)
        }
        BadRequest(toJson(Map("error" -> "invalid_request", "error_description" -> Messages(hasErrors.errors.head.message))))
      },
      success => success.grant_type match {
        case "authorization_code" => 
          {
            val token = AccessTokenService.issueToken(success.code.user, success.client_id.client_id, success.code.scope).toResponseMap
            Ok(toJson(token))
          }
      })
  }

}

object TokenEndpoint extends Controller with TokenEndpoint
