package com.elogiclab.guardbee.core

import play.api.mvc.Action
import play.api.mvc._
import play.api.Application
import securesocial.core.Identity



class SecureSocialWrapperService(application: Application) extends ServerSecurityService {

  
  object provider extends securesocial.core.SecureSocial
  
  

  def SecuredAction(f: AuthWrappedRequest[AnyContent] => Result): Action[AnyContent] = provider.SecuredAction {
    implicit request => 
    f(AuthWrappedRequest(request.user.id.id, request))
  }

}