package com.elogiclab.guardbee.controller

import com.elogiclab.guardbee.auth.OauthError
import play.api.mvc.Controller
import play.api.libs.json.Json._
import play.api.libs.json._
import play.api.mvc.Request

trait Oauth2Endpoint {
  this: Controller =>

  implicit object OauthErrorFormat extends Format[OauthError] {
    def reads(json: JsValue): OauthError = OauthError(
      error = (json \ "error").as[String],
      description = (json \ "description").as[String])
    def writes(e: OauthError): JsValue = JsObject(List("error" -> JsString(e.error), "description" -> JsString(e.description)))
  }

  implicit def OauthErrorImplicits(e: OauthError) = new {

    def toJsonResponse = Status(e.status_code)(toJson(e))
    def toErrorPageResponse(implicit request: Request[_]) = Status(e.status_code)(TemplatesHelper.getAuthErrorPage(e.error, e.description)(request))
  }

}