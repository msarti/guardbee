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

import play.api.Plugin
import play.api.Logger
import scala.collection.mutable.Map
import play.api.i18n.Messages
import play.api.Play

trait ServiceCompanion[A <: BasePlugin] {

  val applicationKey = "guardbee"

  val default: String

  def Unique: Boolean

  lazy val configurationKey = applicationKey + "." + serviceName

  val delegates = Map[String, A]()

  def setService(service: A) = {
    Logger.info("[guardbee] loading service: %s %s".format(this.serviceName, service.id))
    if (service.unique && !delegates.isEmpty) {
      Logger.error("[guardbee] this service must be unique. Please check your play.plugins file: %s %s".format(this.serviceName, service.id))
      throw new RuntimeException(Messages(applicationKey + ".error.duplicateService").format(this.serviceName, service.id))
    }
    delegates += (service.id -> service)
  }

  def serviceName: String

  def getDelegate: Option[A] = {
    if (Unique) {
      delegates.headOption.map(_._2)
    } else {
      Play.current.configuration.getString(configurationKey + ".default") match {
        case None => delegates.get(default)
        case Some(default) => delegates.get(default)
      }
    }
  }
  def getDelegate(id: String): Option[A] = delegates.get(id)

  protected def notInitialized() {
    Logger.error("[guardbee] " + serviceName + " was not initialized. Make sure a " + serviceName + " plugin is specified in your play.plugins file")
    throw new RuntimeException(serviceName + " not initialized")
  }

}