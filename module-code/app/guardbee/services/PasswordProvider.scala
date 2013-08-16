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

abstract class PasswordProvider(application: Application) extends BasePlugin {

  final val unique = false

  override def onStart() = {
    PasswordProvider.setService(this)
  }
  
  def hash(plainpassword:String):Password

  def matches(candidate:String, password:Password): Boolean
  
  
}

object PasswordProvider extends ServiceCompanion[PasswordProvider] {
  val serviceName = "passwordProvider"
  val default = "bcrypt"
  val Unique = true
    
  def hash(plainpassword:String):Password = {
    val delegate = getDelegate.getOrElse {
      notInitialized
      null
    }
    delegate.hash(plainpassword)
  }

}