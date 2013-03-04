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
import com.elogiclab.oauth2.authz.core.AuthorizationRequest
import com.elogiclab.oauth2.authz.core.SimpleAutorizationRequest
import com.elogiclab.oauth2.authz.core.SimpleUserAuthorization

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
    val authcode_duration = Play.current.configuration.getInt("guardbee.authcode_duration").getOrElse(600)
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
            val authz_request_duration = Play.current.configuration.getInt("guardbee.authz_request_duration").getOrElse(600)
            val authzRequest = 
              UserAuthorizationService.saveRequest(SimpleAutorizationRequest(code = UUID.randomUUID().toString, 
                client_id = form_data.client_id.clientId,
                user = request.user,
                response_type = form_data.response_type,
                redirect_uri = form_data.redirect_uri,
                scope = form_data.scope.scope,
                state = form_data.state,
                request_timestamp = DateTime.now,
                request_expiration = DateTime.now.plusSeconds(authz_request_duration)
                ))
            
            Logger.debug("Created new authorization request: " + authzRequest)
            Ok(TemplatesHelper.getAuthRequestForm(form_data.client_id, authzRequest.code))
          }
        case Some(auth) =>
          {
             //We don't need authotization
              Logger.debug("Authorization found for " + form_data.client_id + " - " + auth)
              Logger.info("Redirect to %s ".format(form_data.redirect_uri))
              createAuthCode(request.user, form_data.redirect_uri, form_data.state)
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

  def authz(autzCode: String) = ServerSecurityService.SecuredAction { implicit request =>
    
    UserAuthorizationService.consumeRequest(autzCode) match {
      case None => {
          Logger.error("Authorization request not found")
          BadRequest(TemplatesHelper.getAuthErrorPage("authorization", "guardbee.error.authorization_request_notfound")(request))
      }
      case Some(authzReq) => {
    	  (authzReq.user, authzReq.isExpired) match {
    	    case (request.user, false) => {
    	    	Logger.info("Creating authorization for user "+authzReq.user+" client_id "+authzReq.client_id)
	            UserAuthorizationService.save(SimpleUserAuthorization(client_id = authzReq.client_id, user = request.user, granted_on = DateTime.now))
	            createAuthCode(request.user, authzReq.redirect_uri, authzReq.state)
    	    }
    	    case _ => {
	          Logger.error("Authorization request is not valid")
	          BadRequest(TemplatesHelper.getAuthErrorPage("authorization", "guardbee.error.authorization_request_invalid")(request))
    	    }
    	  }
      }
    }
  }

}