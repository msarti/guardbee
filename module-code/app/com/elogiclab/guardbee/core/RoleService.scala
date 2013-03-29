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

import com.elogiclab.guardbee.auth.UserAccount
import play.api.Application
import play.api.Plugin
import play.api.Logger

trait RoleService {
  
  def getRoles(userAccount: UserAccount): Seq[String]

}



abstract class RoleServicePlugin(application: Application) extends Plugin with RoleService {
  override def onStart() {
    RoleService.setService(this)
  }

}


object RoleService {
  var delegate: Option[RoleService] = None

  def setService(service: RoleService) = {
    delegate = Some(service);
  }
  
  def getRoles(userAccount: UserAccount): Seq[String] = {
    delegate.map(_.getRoles(userAccount)).getOrElse {
      notInitialized()
      Seq.empty
    }

  }
  
  
  
  private def notInitialized() {
    Logger.error("RoleService was not initialized. Make sure a RoleService plugin is specified in your play.plugins file")
    throw new RuntimeException("RoleService not initialized")
  }


}