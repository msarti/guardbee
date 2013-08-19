package guardbee.services.providers

import scala.collection.Seq
import org.joda.time.DateTime
import guardbee.services.ClientIDService
import scala.util.Either
import play.api.Application
import guardbee.services.ClientID
import guardbee.services.Error
import guardbee.services.ClientIDAuthorization
import guardbee.services.ClientIDAuthorization
import guardbee.services.Scope

case class SimpleCliendID(
  clientId: String,
  description: String,
  userId: String,
  homePageUrl: Option[String],
  redirectURIs: Seq[String],
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
    description: String,
    userId: String,
    homePageUrl: Option[String],
    redirectURIs: Seq[String],
    secret: String,
    issuedOn: DateTime = DateTime.now) = {
    SimpleCliendID(
      clientId,
      description,
      userId,
      homePageUrl,
      redirectURIs,
      secret,
      issuedOn)
  }

  def findById(clientId: String): Option[ClientID] = {
    getItem[ClientID, String]("clientIds", clientId)
  }

  def save(clientId: ClientID): Either[Error, Unit] = {
    saveItem[ClientID, String]("clientIds", clientId, clientId.clientId)
    Right()
  }

  def delete(clientId: String): Either[Error, Unit] = {
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

  def saveAuthorization(clientId: ClientIDAuthorization): Either[Error, Unit] = {
    saveItem[ClientIDAuthorization, (String, String)]("clientIdAuthorizations", clientId, (clientId.clientId, clientId.userId))
    Right()
  }

  def deleteAuthorization(clientId: String, userId: String): Either[Error, Unit] = {
    deleteItem[ClientIDAuthorization, (String, String)]("clientIdAuthorizations", (clientId, userId))
    Right()
  }

  //Scope
  def newScope(
    scope: String,
    description: String): Scope = SimpleScope(scope, description)

  def saveScope(scope: Scope): Either[Error, Unit] = {
    saveItem[Scope, String]("scopes", scope, scope.scope)
    Right()
  }

  def findScope(scope: String): Option[Scope] = {
    getItem[Scope, String]("scopes", scope)
  }

  def deleteScope(scope: String): Either[Error, Unit] = {
    deleteItem[Scope, String]("scopes", scope)
    Right()
  }

}