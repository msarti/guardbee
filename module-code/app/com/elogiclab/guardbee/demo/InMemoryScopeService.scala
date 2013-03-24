package com.elogiclab.guardbee.demo

import com.elogiclab.guardbee.core._
import play.api.Application

class InMemoryScopeService (application: Application) extends ScopeServicePlugin(application) {

  private var scopes = Map[String, Scope]()

  def save(scope: Scope): Scope = {
    scopes += (scope.scope -> scope)
    scope
  }

  def findByCode(scope: String): Option[Scope] = {
    scopes.get(scope)
  }

  def delete(scope: String): Unit = {
    scopes -= scope
  }

}
