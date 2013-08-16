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

package guardbee.services

import play.api.Application

abstract class AuthStoreProvider(app: Application) extends BasePlugin {

  final val id = "authstoreService"

  final val unique = true

  def save(key: String, authentication: Authentication): Unit
  def get(key: String): Option[Authentication]
  def delete(key: String): Unit

  override def onStart() = {
    AuthStoreProvider.setService(this)
  }

}

object AuthStoreProvider extends ServiceCompanion[AuthStoreProvider] {
  val serviceName = "authStoreProvider"
  val default = "authstoreService"
  val Unique = true

  def save(key: String, authentication: Authentication): Unit = {
    getDelegate.map(_.save(key, authentication)).getOrElse {
      notInitialized
    }
  }

  def get(key: String): Option[Authentication] = {
    getDelegate.map(_.get(key)).getOrElse {
      notInitialized
      None
    }
  }

  def delete(key: String): Unit = {
    getDelegate.map(_.delete(key)).getOrElse {
      notInitialized
    }
  }

}