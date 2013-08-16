package guardbee.services

import org.joda.time.DateTime
import guardbee.utils.GuardbeeConfiguration
import play.api.Application

trait ClientID {
  def clientId: String
  def description: String
  def userId: String
  def homePageUrl: Option[String]
  def redirectURIs: Seq[String]
  def secret: String
  def issuedOn: DateTime
}

trait ClientIDAuthorization {
  def clientId: String
  def userId: String
  def scope: Seq[String]
  def issuedOn: DateTime
}


abstract class ClientIDService(app: Application) extends BasePlugin with Errors with GuardbeeConfiguration {

  final val unique = true

  def newClientId(
    clientId: String,
    description: String,
    userId: String,
    homePageUrl: Option[String],
    redirectURIs: Seq[String],
    secret: String,
    issuedOn: DateTime = DateTime.now): ClientID

  override def onStart() = {
    ClientIDService.setService(this)
  }

  def findById(clientId: String): Option[ClientID]

  def save(clientId: ClientID): Either[Error, Unit]

  def delete(clientId: String): Either[Error, Unit]

  
  def newClientIdAuthorization(
      clientId: String,
      userId: String,
      scope: Seq[String],
      issuedOn: DateTime   
  ): ClientIDAuthorization

  def saveAuthorization(clientId: ClientIDAuthorization): Either[Error, Unit]

  def deleteAuthorization(clientId: String, userId: String): Either[Error, Unit]

}

object ClientIDService extends ServiceCompanion[ClientIDService] with GuardbeeConfiguration {
  val serviceName = "ClientIDService";
  val default = "";
  val Unique = true

  def findById(clientId: String): Option[ClientID] = {
    getDelegate map(_.findById(clientId)) getOrElse {
      notInitialized
      None
    }
  }

  def save(clientId: ClientID): Either[Error, Unit] = {
    getDelegate map(_.save(clientId)) getOrElse {
      notInitialized
      Left(Errors.InternalServerError)
    }
  }

  def delete(clientId: String): Either[Error, Unit] = {
    getDelegate map(_.delete(clientId)) getOrElse {
      notInitialized
      Left(Errors.InternalServerError)
    }
  }

  def saveAuthorization(auth: ClientIDAuthorization): Either[Error, Unit] = {
    getDelegate map(_.saveAuthorization(auth)) getOrElse {
      notInitialized
      Left(Errors.InternalServerError)
    }
  }

  def deleteAuthorization(clientId: String, userId: String): Either[Error, Unit] = {
    getDelegate map(_.deleteAuthorization(clientId, userId)) getOrElse {
      notInitialized
      Left(Errors.InternalServerError)
    }
  }
}