package com.elogiclab.guardbee.controller

import play.api.mvc._
import play.api.libs.json.Json._
import com.elogiclab.guardbee.core._
import com.elogiclab.guardbee.core.AccessTokenService._
import com.elogiclab.guardbee.format.Formats._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import org.joda.time.Seconds
import org.joda.time.DateTime
import play.api.Logger
import com.elogiclab.guardbee.auth.AuthWrappedRequest
import com.elogiclab.guardbee.auth.ServerSecurityService
import play.api.libs.json._
import com.elogiclab.guardbee.auth.OauthError

case class RequestTokenForm(code: AuthCode, client_id: ClientApplication, client_secret: String, redirect_uri: Option[String], grant_type: String, refresh_token: Option[String])

trait TokenEndpoint extends Oauth2Endpoint {
  this: Controller =>

  implicit object AccessTokenFormat extends Format[AccessToken] {
    def reads(json: JsValue): JsResult[AccessToken] = null
    def writes(token: AccessToken): JsValue = JsObject(List("access_token" -> JsString(token.token),
      "token_type" -> JsString(token.token_type),
      "expires_in" -> JsNumber(Seconds.secondsBetween(DateTime.now, token.token_expiration).getSeconds),
      "refresh_token" -> JsString(token.refresh_token)))

  }

  implicit def tokenImplicits(s: AccessToken) = new {
    def toJsonResponse = Status(OK)(toJson(s))
  }

  def token = Action { implicit request =>

    val requestTokenForm = Form(
      mapping(
        "code" -> of[AuthCode],
        "client_id" -> of[ClientApplication],
        "client_secret" -> text,
        "redirect_uri" -> optional(text),
        "grant_type" -> text.verifying("guardbee.error.invalid_grant_type", v => { v == "authorization_code" || v == "refresh_token" }),
        "refresh_token" -> optional(text))(RequestTokenForm.apply)(RequestTokenForm.unapply)
        .verifying("guardbee.error.invalid_secret", x => x.client_id.client_secret == x.client_secret)
        .verifying("guardbee.error.invalid_redirect_uri", x => {
          (x.grant_type, x.redirect_uri) match {
            case ("authorization_code", None) => false
            case ("authorization_code", Some(ru)) => ru == x.code.redirect_uri
            case _ => true
          }

        })
        .verifying("guardbee.error.missing_refresh_token", v => {
          (v.grant_type, v.refresh_token) match {
            case ("refresh_token", None) => false
            case _ => true
          }
        })
        .verifying("guardbee.error.unauthorized_client_id", v => UserGrantService.isAppGranted(v.client_id.client_id, v.code.user)))

    requestTokenForm.bindFromRequest()(request).fold(
      hasErrors => {
        for (err <- hasErrors.errors) {
          Logger.info(err.message)
        }
        OauthError(description = Messages(hasErrors.errors.head.message)).toJsonResponse
      },
      success => success.grant_type match {
        case "authorization_code" =>
          issueToken(success.code.user, success.client_id.client_id, success.code.scope).toJsonResponse
        case "refresh_token" =>
          refreshToken(success.refresh_token.get).fold(token => token.toJsonResponse, error => error.toJsonResponse)
      })
  }

  def test(client_id: ClientApplication) = Action { implicit request =>
    Ok
  }
  
  def revoke(token: String)  = Action { implicit request =>
    revokeToken(token) match {
      case false => OauthError.INVALID_TOKEN.toJsonResponse
      case _ => Ok(JsObject(List("result" -> JsString("Ok"))))
    }
  }
  
  

}

object TokenEndpoint extends Controller with TokenEndpoint
