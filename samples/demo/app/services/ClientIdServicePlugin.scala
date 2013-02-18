package services

import com.elogiclab.oauth2.authz.core._
import play.api.Application

class InMemoryClientIdService (application: Application) extends ClientIdServicePlugin(application) {
  
  private var ids = Map[String, ClientIdentity]()
  
  
  def save(clientId:ClientIdentity):ClientIdentity = {
    ids += (clientId.clientId -> clientId)
    clientId
  }
  
  def findByClientId(clientId: String): Option[ClientIdentity] = {
    ids.get(clientId)
  }
  
  def delete(clientId:String):Unit = {
    ids -= clientId
  }
}

class InMemoryScopeService (application: Application) extends ScopeServicePlugin(application) {

  private var scopes = Map[String, Scope]()

  def save(scope: Scope): Scope = {
    scopes += (scope.scope -> scope)
    scope
  }

  def findByCode(scope: String): Option[Scope] = {
    scopes.get(scope)
  }

  def delete(scope: String): Unit = {
    scopes -= scope
  }

}