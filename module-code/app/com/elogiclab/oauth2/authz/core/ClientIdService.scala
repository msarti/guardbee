package com.elogiclab.oauth2.authz.core

import play.api.Plugin
import play.api.Application
import play.api.Logger

trait ClientIdentity {
  def clientId: String
  def clientSecret: String
  def applicationName: String
  def applicationDescription: Option[String]
  def redirectURIs: Seq[String]
}

case class SimpleClientId(
    clientId: String, 
    clientSecret: String, 
    applicationName: String, 
    applicationDescription: Option[String], 
    redirectURIs: Seq[String]) extends ClientIdentity

trait ClientIdService {
  
  def save(clientId:ClientIdentity):ClientIdentity
  
  def findByClientId(clientId: String): Option[ClientIdentity]
  
  def delete(clientId:String):Unit
    

}


abstract class ClientIdServicePlugin(application: Application) extends Plugin with ClientIdService {
  
    override def onStart() {
      ClientIdService.setService(this)
    }
  
  
}

object ClientIdService {
  var delegate: Option[ClientIdService] = None
  
  def setService(service: ClientIdService ) = {
    delegate = Some(service);
  }
  
  def save(clientId:ClientIdentity):ClientIdentity = {
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

  def findByClientId(clientId:String): Option[ClientIdentity] = {
    delegate.map(_.findByClientId(clientId)).getOrElse {
      notInitialized()
      None
    }
  }
  
  private def notInitialized() {
    Logger.error("ClientIdService was not initialized. Make sure a ClientIdService plugin is specified in your play.plugins file")
    throw new RuntimeException("ClientIdService not initialized")
  }
  
  
}