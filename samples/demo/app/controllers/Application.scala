package controllers

import play.api._
import play.api.mvc._
import com.elogiclab.guardbee.controller.OAuth2Secured
import com.elogiclab.guardbee.auth.providers.SecureSocialIdentity
import securesocial.core.Identity


object Application extends OAuth2Secured[Identity] with SecureSocialIdentity {
  
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
  
  def getEmail =  withScope("get_profile") { implicit request =>
    Ok
  }
  
  def getOther =  withScope("other_scope") { implicit request =>
    Ok
  }


}