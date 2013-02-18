package com.elogiclab.oauth2.authz.core

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import com.elogiclab.oauth2.format.Formats._
import com.elogiclab.oauth2.controller.TemplatesHelper

object AuthzEndpoint extends Controller {
  def authorizeIfNeeded(form_data: (String, ClientIdentity, String, Scope, Option[String])): Result = {
    Logger.info("Redirect to %s ".format(form_data._3))
    Redirect(form_data._3, Map("code" -> Seq("12345")))
  }

  def auth = ServerSecurityService.SecuredAction { request =>
    Logger.info("In auth...")

    val loginForm = Form(
      tuple(
        "response_type" -> text.verifying("oauth-play2.invalid.response_type", rt => rt == "code"),
        "client_id" -> of[ClientIdentity],
        "redirect_uri" -> text,
        "scope" -> of[Scope],
        "state" -> optional(text)).verifying("oauth-play2.invalid.redirect_uri", x => {
          x._2.redirectURIs.exists(uri => uri == x._3)
        }))

    loginForm.bindFromRequest()(request).fold(
      formWithErrors => BadRequest(TemplatesHelper.getAuthErrorPage(formWithErrors.errors.head.key, formWithErrors.errors.head.message)(request)),
      value => authorizeIfNeeded (value) )
  }

}