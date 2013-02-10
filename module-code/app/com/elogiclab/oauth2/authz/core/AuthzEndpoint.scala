package com.elogiclab.oauth2.authz.core

import play.api.mvc.Action
import play.api._
import play.api.mvc._

object AuthzEndpoint extends Controller {

  def auth(response_type: String, client_id: String, redirect_uri: String, scope: String, state: Option[String]) = Action {

    val cl =
    ClientIdService.findByClientId(client_id).getOrElse(null)    
    Ok
  }

}