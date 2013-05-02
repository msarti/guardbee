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
package com.elogiclab.guardbee.auth

import play.api.{ Plugin, Logger }
import play.api.mvc.AnyContent
import play.api.mvc.Result
import play.api.mvc.Action
import play.api.mvc.Request
import play.api.mvc.WrappedRequest


case class AuthWrappedRequest[A](user: UserAccount, request: Request[A]) extends WrappedRequest(request)

trait ServerSecurityService extends Plugin {

  override def onStart() {
    ServerSecurityService.setService(this)
    Logger.info("[guardbee] Starting ServerAuthService instance: %s".format(getClass.getName))
  }
  
  def SecuredAction(f: AuthWrappedRequest[AnyContent] => Result): Action[AnyContent]
  
  def getAuthenticatedUser[A](implicit request: Request[A]): Option[UserAccount]	

}

object ServerSecurityService {

  var delegate: Option[ServerSecurityService] = None

  def setService(service: ServerSecurityService) = {
    delegate = Some(service);
  }

  def SecuredAction(f: AuthWrappedRequest[AnyContent] => Result): Action[AnyContent] = {
    delegate.map(_.SecuredAction(f)).getOrElse {
      notInitialized()
      null
    }
  }
  
  def getAuthenticatedUser(implicit request: Request[AnyContent]): Option[UserAccount] = {
    delegate.map(_.getAuthenticatedUser).getOrElse {
      notInitialized()
      None
    }
    
  }

  private def notInitialized() {
    Logger.error("ServerSecurityService was not initialized. Make sure a ServerSecurityService plugin is specified in your play.plugins file")
    throw new RuntimeException("ScopeService not initialized")
  }

}