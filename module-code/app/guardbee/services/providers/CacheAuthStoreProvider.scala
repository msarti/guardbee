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

package guardbee.services.providers

import guardbee.services.AuthStoreProvider
import guardbee.services.Authentication
import play.api.Application
import play.api.cache.Cache

class CacheAuthStoreProvider(app: Application) extends AuthStoreProvider(app) {
  
  def save(key: String, authentication: Authentication): Unit = {
    import play.api.Play.current
    Cache.set(key, authentication)
  }
  
  def get(key: String): Option[Authentication] = {
    import play.api.Play.current
    Cache.getAs[Authentication](key)
  }

  def delete(key: String): Unit = {
    import play.api.Play.current
    Cache.remove(key)
  }
}