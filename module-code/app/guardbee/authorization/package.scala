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

package guardbee

import guardbee.services.Authentication
import guardbee.services.Error
import guardbee.services.Errors._
package object authorization {
  
  def isAuthenticated(auth: Option[Authentication]): Either[Error, Unit] = {
    auth.map {
      a => Right()
    } getOrElse Left(AccessDeniedError)
  }
  
  def hasRole(role: String)(auth: Option[Authentication]): Either[Error, Unit] = {
    auth map {
      a => if(a.grants.contains(role)) Right() else Left(AccessDeniedError)
    } getOrElse Left(AccessDeniedError)
  }
  
  def hasScope(scope: String)(auth: Option[Authentication]): Either[Error, Unit] = {
    auth.map {
      a => if(a.scope.map(_.contains(scope)).getOrElse(false)) Right() else Left(AccessDeniedError)
      } getOrElse Left(AccessDeniedError)
  }

  def hasScope(scope: String, role: String)(auth: Option[Authentication]): Either[Error, Unit] = {
    auth.map {
      a => if(
        a.scope.map(_.contains(scope)).getOrElse(false) &&
        a.grants.contains(role)
        ) Right() else Left(AccessDeniedError)
      } getOrElse Left(AccessDeniedError)
  }
}