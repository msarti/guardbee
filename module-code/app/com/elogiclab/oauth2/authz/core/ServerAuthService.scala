package com.elogiclab.oauth2.authz.core

import play.api.{ Plugin, Logger }
import play.api.mvc.WrappedRequest
import play.api.mvc.AnyContent
import play.api.mvc.Result
import play.api.mvc.Action
import play.api.mvc.Request


case class AuthWrappedRequest[A](user: String, request: Request[A]) extends WrappedRequest(request)

trait ServerSecurityService extends Plugin {

  override def onStart() {
    ServerSecurityService.setService(this)
    Logger.info("Starting ServerAuthService instance: %s".format(getClass.getName))
  }

  
  def SecuredAction(f: AuthWrappedRequest[AnyContent] => Result): Action[AnyContent]

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

  private def notInitialized() {
    Logger.error("ServerSecurityService was not initialized. Make sure a ServerSecurityService plugin is specified in your play.plugins file")
    throw new RuntimeException("ScopeService not initialized")
  }

}