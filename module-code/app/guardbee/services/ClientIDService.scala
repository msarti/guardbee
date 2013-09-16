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
  def allowRedirectToLocalhost: Boolean
  def secret: String
  def issuedOn: DateTime
}

trait ClientIDAuthorization {
  def clientId: String
  def userId: String
  def scope: Seq[String]
  def issuedOn: DateTime
}

trait Scope {
  def scope: String
  def description: String
}

abstract class ClientIDService(app: Application) extends BasePlugin with Errors with GuardbeeConfiguration {

  final val unique = true

  def newClientId(
    clientId: String,
    description: String,
    userId: String,
    homePageUrl: Option[String],
    redirectURIs: Seq[String],
    allowRedirectToLocalhost: Boolean,
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
    issuedOn: DateTime): ClientIDAuthorization
    
  def findAuthorization(clientId: String, userId: String): Option[ClientIDAuthorization]

  def saveAuthorization(clientId: ClientIDAuthorization): Either[Error, Unit]

  def deleteAuthorization(clientId: String, userId: String): Either[Error, Unit]

  def newScope(
    scope: String,
    description: String): Scope

  def saveScope(scope: Scope): Either[Error, Unit]

  def findScope(scope: String): Option[Scope]

  def deleteScope(scope: String): Either[Error, Unit]

}

object ClientIDService extends ServiceCompanion[ClientIDService] with GuardbeeConfiguration {
  val serviceName = "ClientIDService";
  val default = "";
  val Unique = true

  def newClientId(
    clientId: String,
    description: String,
    userId: String,
    homePageUrl: Option[String],
    redirectURIs: Seq[String],
    allowRedirectToLocalhost: Boolean,
    secret: String,
    issuedOn: DateTime = DateTime.now): ClientID = {
    getDelegate map (_.newClientId(clientId, description, userId, homePageUrl, redirectURIs, allowRedirectToLocalhost, secret, issuedOn)) getOrElse {
      notInitialized
      null
    }
  }

  def findById(clientId: String): Option[ClientID] = {
    getDelegate map (_.findById(clientId)) getOrElse {
      notInitialized
      None
    }
  }

  def save(clientId: ClientID): Either[Error, Unit] = {
    getDelegate map (_.save(clientId)) getOrElse {
      notInitialized
      Left(Errors.InternalServerError)
    }
  }

  def delete(clientId: String): Either[Error, Unit] = {
    getDelegate map (_.delete(clientId)) getOrElse {
      notInitialized
      Left(Errors.InternalServerError)
    }
  }

  def newClientIdAuthorization(
    clientId: String,
    userId: String,
    scope: Seq[String],
    issuedOn: DateTime = DateTime.now): ClientIDAuthorization = {
    getDelegate map (_.newClientIdAuthorization(clientId, userId, scope, issuedOn)) getOrElse {
      notInitialized
      null
    }
  }

  def findAuthorization(clientId: String, userId: String): Option[ClientIDAuthorization] = {
    getDelegate map (_.findAuthorization(clientId, userId)) getOrElse {
      notInitialized
      None
    }
  }
  
  
  def saveAuthorization(auth: ClientIDAuthorization): Either[Error, Unit] = {
    getDelegate map (_.saveAuthorization(auth)) getOrElse {
      notInitialized
      Left(Errors.InternalServerError)
    }
  }

  def deleteAuthorization(clientId: String, userId: String): Either[Error, Unit] = {
    getDelegate map (_.deleteAuthorization(clientId, userId)) getOrElse {
      notInitialized
      Left(Errors.InternalServerError)
    }
  }

  def newScope(
    scope: String,
    description: String): Scope = {
    getDelegate map (_.newScope(scope, description)) getOrElse {
      notInitialized
      null
    }
  }

  def saveScope(scope: Scope): Either[Error, Unit] = {
    getDelegate map (_.saveScope(scope)) getOrElse {
      notInitialized
      Left(Errors.InternalServerError)
    }

  }

  def findScope(scope: String): Option[Scope] = {
    getDelegate map (_.findScope(scope)) getOrElse {
      notInitialized
      None
    }
  }

  def deleteScope(scope: String): Either[Error, Unit] = {
    getDelegate map (_.deleteScope(scope)) getOrElse {
      notInitialized
      Left(Errors.InternalServerError)
    }

  }

}