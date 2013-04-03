package com.elogiclab.guardbee.service

import com.elogiclab.guardbee.core.ScopeServicePlugin
import play.api.Application
import com.elogiclab.guardbee.core.Scope
import com.elogiclab.guardbee.model.SimpleScope

class DefaultScopeService (application: Application) extends ScopeServicePlugin(application) {
  
  def save(scope: Scope): Scope = {
    SimpleScope.create(scope)
  }

  def findByCode(scope: String): Option[Scope] = {
    SimpleScope.findByCode(scope)
  }

  def delete(scope: String): Unit = {
    SimpleScope.delete(scope)
  }
}