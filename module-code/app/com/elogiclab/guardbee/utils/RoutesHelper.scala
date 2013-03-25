package com.elogiclab.guardbee.utils

import play.Play
import play.api.mvc.Call

object RoutesHelper {
  lazy val conf = play.api.Play.current.configuration

  lazy val aep = Play.application.classloader.loadClass("com.elogiclab.guardbee.controller.ReverseAuthzEndpoint")
  lazy val authEndpointMethods = aep.newInstance().asInstanceOf[{
    def auth(): Call
    def authz(authzCode: String): Call
  }]
  
  def authz(authzCode: String) = authEndpointMethods.authz(authzCode)

}