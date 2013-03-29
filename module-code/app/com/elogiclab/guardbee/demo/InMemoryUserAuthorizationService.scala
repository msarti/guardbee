package com.elogiclab.guardbee.demo

import com.elogiclab.guardbee.core._
import play.api.Application

class InMemoryUserAuthorizationService(application: Application) extends GrantedAuthorizationServicePlugin(application) {

  private var authotizations = Map[String, GrantedAuthorization]()
  private var authzRequests = Map[String, AuthorizationRequest]()

  
  def save(authorization: GrantedAuthorization): GrantedAuthorization = {
    authotizations += ( (authorization.client_id + authorization.user) -> authorization)
    authorization
  }

  def findByClientIdAndUser(clientId: String, userId: String): Option[GrantedAuthorization] = {
    authotizations.get((clientId + userId))
  }

  def delete(clientId: String, userId: String): Unit = {
    authotizations -= (clientId + userId)
  }


  def saveRequest(authzRequest: AuthorizationRequest): AuthorizationRequest = {
    authzRequests += (authzRequest.code -> authzRequest)
    authzRequest
  }
  
  def consumeRequest(requestCode: String): Option[AuthorizationRequest] = {
    authzRequests.get(requestCode) match {
      case None => None
      case Some(authzReq) => {
        authzRequests -= requestCode
        Some(authzReq)
      }
    }
  }

  
}
