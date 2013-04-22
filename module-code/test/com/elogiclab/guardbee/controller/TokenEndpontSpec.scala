package com.elogiclab.guardbee.controller

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class TokenEndpontSpec extends Specification {

  "Application" should {

    "respond to the index Action" in {
      val result = com.elogiclab.guardbee.controller.TokenEndpoint.token()(FakeRequest())

      status(result) must equalTo(OK)
      contentType(result) must beSome("text/html")
      charset(result) must beSome("utf-8")
      contentAsString(result) must contain("Hello Bob")
    }
  }
}