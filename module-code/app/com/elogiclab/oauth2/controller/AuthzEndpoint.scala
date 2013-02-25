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

case class AuthForm(response_type: String, client_id: ClientIdentity, redirect_uri: String, scope: Scope, state: Option[String])

object AuthzEndpoint extends Controller { 

  val loginForm = Form(
    mapping(
      "response_type" -> text.verifying("oauth-play2.invalid.response_type", rt => rt == "code"),
      "client_id" -> of[ClientIdentity],
      "redirect_uri" -> text,
      "scope" -> of[Scope],
      "state" -> optional(text))(AuthForm.apply)(AuthForm.unapply)
      .verifying("oauth-play2.invalid.redirect_uri", x => {
        x.client_id.redirectURIs.exists(uri => uri == x.redirect_uri)
      }))

  def auth = ServerSecurityService.SecuredAction { implicit request =>
    Logger.debug("In auth..." + request.user)

    def authorizeIfNeeded(form_data: AuthForm): Result = {
          UserAuthorizationService.findByClientIdAndUser(form_data.client_id.clientId, request.user) match {
            case None => // Authorization needed 
              {
                Logger.debug("User "+request.user+" need to authorize app "+form_data.client_id)
                Ok(TemplatesHelper.getAuthRequestForm(form_data))
              }
            case Some(auth) => 
              {
                if(auth.enabled) { //We don't need authotization
	                Logger.debug("Authorization found for "+form_data.client_id+" - "+auth)
	                Logger.info("Redirect to %s ".format(form_data.redirect_uri))
	                Redirect(form_data.redirect_uri, Map("code" -> Seq("12345")))
                } else { //We need authotization
	                Logger.debug("User "+request.user+" need to authorize app "+form_data.client_id)
	                Ok(TemplatesHelper.getAuthRequestForm(form_data))
                }
              }
          }

    }

    loginForm.bindFromRequest()(request).fold(
      formWithErrors => //Validation errors
      {
        Logger.info("There are errors in request: "+formWithErrors)  
        BadRequest(TemplatesHelper.getAuthErrorPage(formWithErrors.errors.head.key, formWithErrors.errors.head.message)(request))
      },
      
      value => authorizeIfNeeded(value))
  }

  
  def authz = ServerSecurityService.SecuredAction { implicit request =>
    Ok
  }
  
}