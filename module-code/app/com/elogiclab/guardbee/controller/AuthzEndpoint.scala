package com.elogiclab.guardbee.controller

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import com.elogiclab.guardbee.format.Formats._
import play.api.i18n.Messages
import java.util.UUID
import org.joda.time.DateTime
import com.elogiclab.guardbee.core._
import com.elogiclab.guardbee.auth.ServerSecurityService
import com.elogiclab.guardbee.auth.AuthWrappedRequest

case class AuthForm(response_type: String, client_id: ClientApplication, redirect_uri: String, scope: Seq[Scope], state: Option[String], code: Option[String], authorized: Option[String])

object AuthzEndpoint extends Controller {

  
  val loginForm = Form(
    mapping(
      "response_type" -> text.verifying("guardbee.error.invalid_response_type", rt => rt == "code"),
      "client_id" -> of[ClientApplication],
      "redirect_uri" -> text,
      "scope" -> of[Seq[Scope]],
      "state" -> optional(text),
      "code" -> optional(text),
      "authorized" -> optional(text))(AuthForm.apply)(AuthForm.unapply)
      .verifying("guardbee.error.invalid_redirect_uri", x => {
        x.client_id.redirect_uris.exists(uri => uri == x.redirect_uri)
      }))

  private def sendAuthCode(redirect_uri: String, scope: Seq[Scope], state: Option[String])(implicit request: AuthWrappedRequest[AnyContent]) = {
    val code = AuthCodeService.issueCode(redirect_uri, scope)

    val params = Map("code" -> Seq(code.code)) ++ (state match {
      case None => Map()
      case Some(value) => Map("state" -> Seq(value))
    })

    Redirect(redirect_uri, params)

  }

  def auth = ServerSecurityService.SecuredAction { implicit request =>
 
    def authorizeIfNeeded(form_data: AuthForm): Result = {
      GrantedAuthorizationService.findByClientIdAndUser(form_data.client_id.client_id, request.user.username) match {
        case None => // Authorization needed 
          {
            Logger.debug("User " + request.user + " need to authorize app " + form_data.client_id)
            val authzRequest =
            GrantedAuthorizationService.makeUserAuthRequest(
                form_data.client_id.client_id, form_data.response_type, form_data.redirect_uri, form_data.scope, form_data.state)
            
            Logger.debug("Created new authorization request: " + authzRequest)
            Ok(TemplatesHelper.getAuthRequestForm(form_data.client_id, authzRequest.code))
          }
        case Some(auth) =>
          {
             //We don't need authotization
              Logger.debug("Authorization found for " + form_data.client_id + " - " + auth)
              Logger.info("Redirect to %s ".format(form_data.redirect_uri))
              sendAuthCode(form_data.redirect_uri, form_data.scope, form_data.state)
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
    
    GrantedAuthorizationService.consumeRequest(autzCode) match {
      case None => {
          Logger.error("Authorization request not found")
          BadRequest(TemplatesHelper.getAuthErrorPage("authorization", "guardbee.error.authorization_request_notfound")(request))
      }
      case Some(authzReq) => {
    	  (authzReq.user, authzReq.isExpired) match {
    	    case (request.user, false) => {
    	    	Logger.info("Creating authorization for user "+authzReq.user+" client_id "+authzReq.client_id)
    	    	GrantedAuthorizationService.grantAuthorization(authzReq.client_id, authzReq.scope)
	            sendAuthCode(authzReq.redirect_uri, authzReq.scope, authzReq.state)
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