/**
 * Copyright 2013 Marco Sarti - twitter: @marconesarti
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.elogiclab.guardbee.auth.providers

import play.api.mvc.Action
import play.api.mvc._
import play.api.Application
import com.elogiclab.guardbee.auth.AuthWrappedRequest
import com.elogiclab.guardbee.auth.ServerSecurityService
import com.elogiclab.guardbee.auth.UserAccount
import securesocial.core.Identity
import com.elogiclab.guardbee.auth.UserAccount
import com.elogiclab.guardbee.auth.UserAuthorization
import securesocial.core.SecureSocial
import com.elogiclab.guardbee.auth.SimpleUserAccount
import SecureSocialImplicits._


trait SecureSocialUserAccount extends UserAccount with Identity

class SecureSocialWrapperService(application: Application) extends ServerSecurityService {


  object provider extends securesocial.core.SecureSocial

  def SecuredAction(f: AuthWrappedRequest[AnyContent] => Result): Action[AnyContent] = provider.SecuredAction {
    implicit request =>
      f(AuthWrappedRequest(request.user.toUserAccount, request))
  }

  def SecuredAction(authorization: UserAuthorization)(f: AuthWrappedRequest[AnyContent] => Result): Action[AnyContent] = provider.SecuredAction(false, new SecureSocialWrappedAuthorization(authorization)) {
    implicit request =>
      f(AuthWrappedRequest(request.user.toUserAccount, request))
  }

  def getAuthenticatedUser[A](implicit request: Request[A]): Option[UserAccount] = {
    SecureSocial.currentUser match {
      case None => None
      case Some(identity) => Some(identity.toUserAccount)

    }

  }

}