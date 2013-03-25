package com.elogiclab.guardbee.auth.providers

import play.api.mvc.Action
import play.api.mvc._
import play.api.Application
import com.elogiclab.guardbee.auth.AuthWrappedRequest
import com.elogiclab.guardbee.auth.ServerSecurityService
import com.elogiclab.guardbee.auth.UserAccount
import securesocial.core.Identity
import com.elogiclab.guardbee.auth.UserAccount

trait SecureSocialUserAccount extends UserAccount with Identity

class SecureSocialWrapperService(application: Application) extends ServerSecurityService {

  
  object provider extends securesocial.core.SecureSocial
  
  
  def SecuredAction(f: AuthWrappedRequest[AnyContent] => Result): Action[AnyContent] = provider.SecuredAction {
    implicit request => 
    f(AuthWrappedRequest(UserAccount(username = request.user.id.id, firstName = request.user.firstName, lastName = request.user.lastName, avatarUrl = request.user.avatarUrl), request))
  }

}