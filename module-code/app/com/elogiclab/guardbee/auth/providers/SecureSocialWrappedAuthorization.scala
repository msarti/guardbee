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

import com.elogiclab.guardbee.auth.UserAuthorization
import securesocial.core.Authorization
import securesocial.core.Identity
import com.elogiclab.guardbee.auth.UserAccount

case class SecureSocialWrappedAuthorization(authorization:UserAuthorization) extends Authorization {
  def isAuthorized(user: Identity): Boolean = authorization.isAuthorized(UserAccount(username = user.id.id, firstName = user.firstName, lastName = user.lastName, avatarUrl = user.avatarUrl))
}