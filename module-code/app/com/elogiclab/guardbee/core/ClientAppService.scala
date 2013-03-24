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

import play.api.Plugin
import play.api.Application
import play.api.Logger
import play.api.mvc.AnyContent
import java.util.UUID
import org.joda.time.DateTime

trait ClientApplication {
  def client_id: String
  def client_secret: String
  def owner_user: String
  def app_name: String
  def app_description: Option[String]
  def redirect_uris: Seq[String]
  def issued_on: DateTime
}

case class SimpleClientApplication(
    client_id: String, 
    client_secret: String, 
    owner_user: String,
    app_name: String, 
    app_description: Option[String], 
    redirect_uris: Seq[String],
    issued_on: DateTime) extends ClientApplication

trait ClientAppService {
  
  def save(clientId:ClientApplication):ClientApplication
  
  def findByClientId(clientId: String): Option[ClientApplication]
  
  def delete(clientId:String):Unit
    

}


abstract class ClientAppServicePlugin(application: Application) extends Plugin with ClientAppService {
  
    override def onStart() {
      ClientAppService.setService(ClientAppServicePlugin.this)
    }
  
  
}

object ClientAppService {
  var delegate: Option[ClientAppService] = None
  
  def setService(service: ClientAppService ) = {
    delegate = Some(service);
  }
  
  def save(clientId:ClientApplication):ClientApplication = {
    delegate.map(_.save(clientId)).getOrElse {
      notInitialized()
      clientId
    }
  }
  
  def delete(clientId:String):Unit = {
    delegate.map(_.delete(clientId)).getOrElse {
      notInitialized()
    }
  }

  def findByClientId(clientId:String): Option[ClientApplication] = {
    delegate.map(_.findByClientId(clientId)).getOrElse {
      notInitialized()
      None
    }
  }
  
  def issueClientApplication(app_name:String, app_description: Option[String], redirect_uris:Seq[String])(implicit request: AuthWrappedRequest[AnyContent]) = {
    save(SimpleClientApplication(
    		client_id = UUID.randomUUID().toString(),
    		client_secret = UUID.randomUUID().toString(),
    		owner_user = request.user,
    		app_name = app_name,
    		app_description = app_description,
    		redirect_uris = redirect_uris,
    		issued_on = DateTime.now
     ))
  }
  
  
  
  
  
  private def notInitialized()  = {
    Logger.error("ClientAppService was not initialized. Make sure a ClientAppService plugin is specified in your play.plugins file")
    throw new RuntimeException("ClientIdService not initialized")
  }
  
  
}