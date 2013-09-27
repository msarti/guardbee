package guardbee.services.providers

import scala.collection.Seq
import org.joda.time.DateTime
import guardbee.services.ClientIDService
import scala.util.Either
import play.api.Application
import guardbee.services.ClientID
import guardbee.services.ClientIDAuthorization
import guardbee.services.ClientIDAuthorization
import guardbee.services.Scope
import guardbee.utils.GuardbeeError

case class SimpleCliendID(
  clientId: String,
  title: String,
  description: String,
  userId: String,
  homePageUrl: Option[String],
  redirectURIs: Seq[String],
  allowRedirectToLocalhost: Boolean,
  secret: String,
  issuedOn: DateTime = DateTime.now) extends ClientID

case class SimpleClientIDAuthorization(
  clientId: String,
  userId: String,
  scope: Seq[String],
  issuedOn: DateTime) extends ClientIDAuthorization

case class SimpleScope(
    scope: String,
    description: String
) extends Scope
  


class CacheClientIDService(app: Application) extends ClientIDService(app) with BaseCacheStore {

  val id = "CacheClientIDService"

  def newClientId(
    clientId: String,
    title: String,
    description: String,
    userId: String,
    homePageUrl: Option[String],
    redirectURIs: Seq[String],
    allowRedirectToLocalhost: Boolean,
    secret: String,
    issuedOn: DateTime = DateTime.now) = {
    SimpleCliendID(
      clientId,
      description,
      title,
      userId,
      homePageUrl,
      redirectURIs,
      allowRedirectToLocalhost,
      secret,
      issuedOn)
  }

  def findById(clientId: String): Option[ClientID] = {
    getItem[ClientID, String]("clientIds", clientId)
  }

  def save(clientId: ClientID): Either[GuardbeeError, Unit] = {
    saveItem[ClientID, String]("clientIds", clientId, clientId.clientId)
    Right()
  }

  def delete(clientId: String): Either[GuardbeeError, Unit] = {
    deleteItem[ClientID, String]("clientIds", clientId)
    Right()
  }

  def newClientIdAuthorization(
    clientId: String,
    userId: String,
    scope: Seq[String],
    issuedOn: DateTime): ClientIDAuthorization = {
    SimpleClientIDAuthorization(
      clientId,
      userId,
      scope,
      issuedOn)
  }

  def findAuthorization(clientId: String, userId: String): Option[ClientIDAuthorization] = {
    getItem[ClientIDAuthorization, (String, String)]("clientIdAuthorizations", (clientId, userId))
  }
  
  
  def saveAuthorization(clientId: ClientIDAuthorization): Either[GuardbeeError, Unit] = {
    saveItem[ClientIDAuthorization, (String, String)]("clientIdAuthorizations", clientId, (clientId.clientId, clientId.userId))
    Right()
  }

  def deleteAuthorization(clientId: String, userId: String): Either[GuardbeeError, Unit] = {
    deleteItem[ClientIDAuthorization, (String, String)]("clientIdAuthorizations", (clientId, userId))
    Right()
  }

  //Scope
  def newScope(
    scope: String,
    description: String): Scope = SimpleScope(scope, description)

  def saveScope(scope: Scope): Either[GuardbeeError, Unit] = {
    saveItem[Scope, String]("scopes", scope, scope.scope)
    Right()
  }

  def findScope(scope: String): Option[Scope] = {
    getItem[Scope, String]("scopes", scope)
  }

  def deleteScope(scope: String): Either[GuardbeeError, Unit] = {
    deleteItem[Scope, String]("scopes", scope)
    Right()
  }

}