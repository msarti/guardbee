package com.elogiclab.guardbee.demo

import com.elogiclab.guardbee.core._
import play.api.Application

class InMemoryClientAppService  (application: Application) extends ClientAppServicePlugin(application) {
  
  
  private var ids = Map[String, ClientApplication]()
  
  
  def save(clientId: ClientApplication):ClientApplication = {
    ids += (clientId.client_id -> clientId)
    clientId
  }
  
  def findByClientId(clientId: String): Option[ClientApplication] = {
    ids.get(clientId)
  }
  
  def delete(clientId:String):Unit = {
    ids -= clientId
  }
  
}