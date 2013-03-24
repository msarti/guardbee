package com.elogiclab.guardbee.core

import play.api.Logger
import play.api.Application
import play.api.Plugin

trait Scope {
  def scope: String
  def description: String
}

trait ScopeService {

  def save(scope: Scope): Scope

  def findByCode(scope: String): Option[Scope]

  def delete(scope: String): Unit

}

case class SimpleScope(scope: String, description: String) extends Scope

abstract class ScopeServicePlugin(application: Application) extends Plugin with ScopeService {

  override def onStart() {
    ScopeService.setService(this)
  }

}

object ScopeService {
  var delegate: Option[ScopeService] = None

  def setService(service: ScopeService) = {
    delegate = Some(service);
  }

  def save(scope: Scope): Scope = {
    delegate.map(_.save(scope)).getOrElse {
      notInitialized()
      scope
    }
  }

  def delete(scope: String): Unit = {
    delegate.map(_.delete(scope)).getOrElse {
      notInitialized()
    }
  }

  def findByCode(scope: String): Option[Scope] = {
    delegate.map(_.findByCode(scope)).getOrElse {
      notInitialized()
      None
    }
  }

  private def notInitialized() {
    Logger.error("ScopeService was not initialized. Make sure a ScopeService plugin is specified in your play.plugins file")
    throw new RuntimeException("ScopeService not initialized")
  }

}