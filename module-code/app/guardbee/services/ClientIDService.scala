package guardbee.services

import org.joda.time.DateTime
import guardbee.utils.GuardbeeConfiguration
import play.api.Application
import guardbee.utils.GuardbeeError

trait ClientID {
  def clientId: String
  def title: String
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

abstract class ClientIDService(app: Application) extends BasePlugin with GuardbeeConfiguration {

  final val unique = true

  def newClientId(
    clientId: String,
    title: String,
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

  def save(clientId: ClientID): Either[GuardbeeError, Unit]

  def delete(clientId: String): Either[GuardbeeError, Unit]

  def newClientIdAuthorization(
    clientId: String,
    userId: String,
    scope: Seq[String],
    issuedOn: DateTime): ClientIDAuthorization
    
  def findAuthorization(clientId: String, userId: String): Option[ClientIDAuthorization]

  def saveAuthorization(clientId: ClientIDAuthorization): Either[GuardbeeError, Unit]

  def deleteAuthorization(clientId: String, userId: String): Either[GuardbeeError, Unit]

  def newScope(
    scope: String,
    description: String): Scope

  def saveScope(scope: Scope): Either[GuardbeeError, Unit]

  def findScope(scope: String): Option[Scope]

  def deleteScope(scope: String): Either[GuardbeeError, Unit]

}

object ClientIDService extends ServiceCompanion[ClientIDService] with GuardbeeConfiguration {
  val serviceName = "ClientIDService";
  val default = "";
  val Unique = true

  def newClientId(
    clientId: String,
    title: String,
    description: String,
    userId: String,
    homePageUrl: Option[String],
    redirectURIs: Seq[String],
    allowRedirectToLocalhost: Boolean,
    secret: String,
    issuedOn: DateTime = DateTime.now): ClientID = {
    getDelegate map (_.newClientId(clientId, title, description, userId, homePageUrl, redirectURIs, allowRedirectToLocalhost, secret, issuedOn)) getOrElse {
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

  def save(clientId: ClientID): Either[GuardbeeError, Unit] = {
    getDelegate map (_.save(clientId)) getOrElse {
      notInitialized
      Left(GuardbeeError.InternalServerError)
    }
  }

  def delete(clientId: String): Either[GuardbeeError, Unit] = {
    getDelegate map (_.delete(clientId)) getOrElse {
      notInitialized
      Left(GuardbeeError.InternalServerError)
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
  
  
  def saveAuthorization(auth: ClientIDAuthorization): Either[GuardbeeError, Unit] = {
    getDelegate map (_.saveAuthorization(auth)) getOrElse {
      notInitialized
      Left(GuardbeeError.InternalServerError)
    }
  }

  def deleteAuthorization(clientId: String, userId: String): Either[GuardbeeError, Unit] = {
    getDelegate map (_.deleteAuthorization(clientId, userId)) getOrElse {
      notInitialized
      Left(GuardbeeError.InternalServerError)
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

  def saveScope(scope: Scope): Either[GuardbeeError, Unit] = {
    getDelegate map (_.saveScope(scope)) getOrElse {
      notInitialized
      Left(GuardbeeError.InternalServerError)
    }

  }

  def findScope(scope: String): Option[Scope] = {
    getDelegate map (_.findScope(scope)) getOrElse {
      notInitialized
      None
    }
  }

  def deleteScope(scope: String): Either[GuardbeeError, Unit] = {
    getDelegate map (_.deleteScope(scope)) getOrElse {
      notInitialized
      Left(GuardbeeError.InternalServerError)
    }

  }

}