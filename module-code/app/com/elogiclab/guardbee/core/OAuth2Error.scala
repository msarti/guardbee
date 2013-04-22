package com.elogiclab.guardbee.core

import play.api.libs.json._

case class OAuth2Error(error_code: Int, error_message: String) {
  implicit object OAuth2ErrorFormat extends Format[OAuth2Error] {
    
    def writes(e: OAuth2Error): JsValue = {
      JsObject(List("error_code" -> JsNumber(e.error_code), "error_message" -> JsString(e.error_message)))
    }
    def reads(json: JsValue): OAuth2Error = OAuth2Error((json \ "error_code").as[Int], (json \ "error_message").as[String])
  }

  lazy val json = Json.toJson(this)
}

object OAuth2Error {

}