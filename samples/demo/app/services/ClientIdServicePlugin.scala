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


class InMemoryUserAuthorizationService(application: Application) extends UserAuthorizationServicePlugin(application) {

  private var authotizations = Map[String, UserAuthorization]()

  
  def save(authorization: UserAuthorization): UserAuthorization = {
    authotizations += ( (authorization.clientId + authorization.userId) -> authorization)
    authorization
  }

  def findByClientIdAndUser(clientId: String, userId: String): Option[UserAuthorization] = {
    authotizations.get((clientId + userId))
  }

  def delete(clientId: String, userId: String): Unit = {
    authotizations -= (clientId + userId)
  }

  def enable(clientId: String, userId: String): Option[UserAuthorization] = {
    authotizations.get((clientId + userId)) match {
      case None => None
      case Some(value) => {
        val newValue = SimpleUserAuthorization(value.clientId, value.userId, value.verificationCode, value.grantedOn, true)
        authotizations += ( (newValue.clientId + newValue.userId) -> newValue)
        Some(newValue)
      }
      
    }
  }

  
}

class InMemoryAuthCodeService(application: Application) extends AuthCodeServicePlugin(application) {
  
  private var codes = Map[String, AuthCode]()

  
  def save(authCode: AuthCode): AuthCode = {
    codes += (authCode.code -> authCode)
    authCode
  }

  /**
   * @param code 
   * @return
   */
  def consume(code: String): Option[AuthCode] = {
    codes.get(code) match {
      case None => None
      case Some(authCode) => {
    	codes -= code
        Some(authCode)
      }
      
    }
  }

}

