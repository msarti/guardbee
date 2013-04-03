package com.elogiclab.guardbee.service

import com.elogiclab.guardbee.core._
import com.elogiclab.guardbee.model._

import play.api.Application

class DefaultUserGrantService(application: Application) extends UserGrantServicePlugin(application) {

  def save(grant: UserGrant): UserGrant = SimpleUserGrant.create(grant)

  def saveRequest(req: AuthorizationRequest): AuthorizationRequest = SimpleAutorizationRequest.create(req)

  def consumeRequest(code: String): Option[AuthorizationRequest] = SimpleAutorizationRequest.consume(code)

  def findByClientIdAndUser(client_id: String, user: String): Option[UserGrant] = SimpleUserGrant.findByClientIdAndUser(client_id, user)

  def delete(client_id: String, user: String): Unit = SimpleUserGrant.delete(client_id, user)


}