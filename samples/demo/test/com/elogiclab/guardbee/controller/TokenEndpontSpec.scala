package com.elogiclab.guardbee.controller

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.mvc.AnyContentAsFormUrlEncoded
import com.elogiclab.guardbee.core.AuthCodeService
import com.elogiclab.guardbee.core.ScopeService

class TokenEndpontSpec extends Specification {

  "Application" should {

    "respond to the index Action" in {
      running(FakeApplication()) {
        
        val request = new AnyContentAsFormUrlEncoded(
          Map("client_id" -> Seq("client_id"),
            "client_secret" -> Seq("client_secret"),
            "code" -> Seq("code"),
            "grant_type" -> Seq("authorization_code")))
        val headers = FakeHeaders(Map())

        val result = com.elogiclab.guardbee.controller.TokenEndpoint.token()(new FakeRequest("GET", "/", headers, request))
        print(contentAsString(result))

        status(result) must equalTo(400)
        contentType(result) must beSome("application/json")
        charset(result) must beSome("utf-8")
        contentAsString(result) must contain("error")
      }
    }
  }
}