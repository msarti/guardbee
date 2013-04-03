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

import com.elogiclab.guardbee.controller.OAuth2Secured
import play.api.mvc.AnyContent
import securesocial.core.Identity
import securesocial.core.UserService
import securesocial.core.UserServicePlugin
import play.api.Logger

trait SecureSocialIdentity  {
  
  import play.api.Play._
  
  val plugin:UserServicePlugin = {
    application.plugin[UserServicePlugin] match {
      case None => {
        Logger.error("UserService was not initialized. Make sure a UserService plugin is specified in your play.plugins file")
        throw new RuntimeException("UserService not initialized")
      }
      case Some(plugin) => plugin
    }
  }
  
  
  def findUser(user_id: String): Option[Identity] = {
      plugin.findByEmailAndProvider(user_id, "userpass")
  }

}