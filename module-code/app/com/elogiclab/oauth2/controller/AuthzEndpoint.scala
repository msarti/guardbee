package com.elogiclab.oauth2.controller

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import com.elogiclab.oauth2.format.Formats._
import com.elogiclab.oauth2.authz.core.ClientIdentity
import com.elogiclab.oauth2.authz.core.Scope
import com.elogiclab.oauth2.authz.core.ServerSecurityService
import com.elogiclab.oauth2.authz.core.UserAuthorizationService
import play.api.i18n.Messages
import com.elogiclab.oauth2.authz.core.AuthCodeService
import com.elogiclab.oauth2.authz.core.AuthCode
import com.elogiclab.oauth2.authz.core.SimpleAuthCode
import java.util.UUID
import org.joda.time.DateTime
import com.elogiclab.oauth2.authz.core.SimpleUserAuthorization
import com.elogiclab.oauth2.authz.core.AuthWrappedRequest

case class AuthForm(response_type: String, client_id: ClientIdentity, redirect_uri: String, scope: Scope, state: Option[String], code: Option[String], authorized: Option[String])

object AuthzEndpoint extends Controller {

  val loginForm = Form(
    mapping(
      "response_type" -> text.verifying("oauth-play2.invalid.response_type", rt => rt == "code"),
      "client_id" -> of[ClientIdentity],
      "redirect_uri" -> text,
      "scope" -> of[Scope],
      "state" -> optional(text),
      "code" -> optional(text),
      "authorized" -> optional(text))(AuthForm.apply)(AuthForm.unapply)
      .verifying("oauth-play2.invalid.redirect_uri", x => {
        x.client_id.redirectURIs.exists(uri => uri == x.redirect_uri)
      }))

  private def createAuthCode(user: String, redirect_uri: String, state: Option[String]) = {
    val authcode_duration = Play.current.configuration.getInt("play-oaut2.authcode.duration").getOrElse(60)
    val code =
      AuthCodeService.save(SimpleAuthCode(UUID.randomUUID.toString, user, DateTime.now, DateTime.now.plusSeconds(authcode_duration)))

    val params = Map("code" -> Seq(code.code)) ++ (state match {
      case None => Map()
      case Some(value) => Map("state" -> Seq(value))
    })

    Redirect(redirect_uri, params)

  }

  def auth = ServerSecurityService.SecuredAction { implicit request =>
    Logger.debug("In auth..." + request.user)

    def authorizeIfNeeded(form_data: AuthForm): Result = {
      UserAuthorizationService.findByClientIdAndUser(form_data.client_id.clientId, request.user) match {
        case None => // Authorization needed 
          {
            Logger.debug("User " + request.user + " need to authorize app " + form_data.client_id)
            val newauth =
              UserAuthorizationService.save(SimpleUserAuthorization(clientId = form_data.client_id.clientId, userId = request.user, verificationCode = UUID.randomUUID().toString(), grantedOn = DateTime.now(), enabled = false))
            Logger.debug("Saved authorization: " + newauth)
            Ok(TemplatesHelper.getAuthRequestForm(form_data.copy(code = Some(newauth.verificationCode))))
          }
        case Some(auth) =>
          {
            if (auth.enabled) { //We don't need authotization
              Logger.debug("Authorization found for " + form_data.client_id + " - " + auth)
              Logger.info("Redirect to %s ".format(form_data.redirect_uri))
              createAuthCode(request.user, form_data.redirect_uri, form_data.state)
            } else { //We need authotization
              Logger.debug("User " + request.user + " need to authorize app " + form_data.client_id)
              Ok(TemplatesHelper.getAuthRequestForm(form_data.copy(code = Some(auth.verificationCode))))
            }
          }
      }

    }

    loginForm.bindFromRequest()(request).fold(
      formWithErrors => //Validation errors
        {
          Logger.info("There are errors in request: " + formWithErrors)
          BadRequest(TemplatesHelper.getAuthErrorPage(formWithErrors.errors.head.key, formWithErrors.errors.head.message)(request))
        },

      value => authorizeIfNeeded(value))
  }

  def authz = ServerSecurityService.SecuredAction { implicit request =>

    loginForm.bindFromRequest()(request).fold(
      formWithErrors => //Validation errors
        {
          Logger.info("There are errors in request: " + formWithErrors)
          BadRequest(TemplatesHelper.getAuthErrorPage(formWithErrors.errors.head.key, formWithErrors.errors.head.message)(request))
        },

      value =>
        {
          UserAuthorizationService.findByClientIdAndUser(value.client_id.clientId, request.user) match {
            case None => {
              Logger.error("Authorization not found")
              BadRequest(TemplatesHelper.getAuthErrorPage("authorization", "guardbee.error.authorization_not_found")(request))
            }
            case Some(auth) => {
              Logger.debug("Test if " + auth.verificationCode + " is equal to " + value.code)
              if (auth.verificationCode == value.code.getOrElse("")) {
                UserAuthorizationService.enable(value.client_id.clientId, request.user)
                createAuthCode(request.user, value.redirect_uri, value.state)
              } else {
                Logger.error("The " + value.code + " code is not valid")
                BadRequest(TemplatesHelper.getAuthErrorPage("authorization", "guardbee.error.invalid_verification_code")(request))
              }
            }

          }
        })
  }

}