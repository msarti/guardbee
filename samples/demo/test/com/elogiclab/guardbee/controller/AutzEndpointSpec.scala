package com.elogiclab.guardbee.controller

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.mvc.AnyContentAsFormUrlEncoded
import com.elogiclab.guardbee.core.ScopeService
import com.elogiclab.guardbee.service.DefaultScopeService


class AutzEndpointSpec  extends Specification {
  
  
  "AutzEndpointSpec" should {
    
    "Must redirect to login" in {
      running(FakeApplication()) {
	      val request = new AnyContentAsFormUrlEncoded(Map())
	      val headers = FakeHeaders(Map())
	      
	      
	      val result = com.elogiclab.guardbee.controller.AuthzEndpoint.auth()(new FakeRequest("GET", "/", headers, request ))
	      print(contentAsString(result))
	      status(result) must equalTo(SEE_OTHER)
      }
      
    }
    
  }

}