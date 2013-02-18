package com.elogiclab.oauth2.authz.core

import play.api.mvc.Action
import play.api.mvc._
import play.api.Application

class SecureSocialWrapperService(application: Application) extends ServerSecurityService {

  
  object provider extends securesocial.core.SecureSocial
  

  def SecuredAction(f: WrappedRequest[AnyContent] => Result): Action[AnyContent] = provider.SecuredAction(f)

}