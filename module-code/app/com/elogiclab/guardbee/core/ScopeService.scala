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