package guardbee.services

import play.api.Application
import org.joda.time.DateTime
import java.util.UUID
import guardbee.utils.GuardbeeConfiguration
import org.joda.time.Minutes
import guardbee.utils.GuardbeeError

trait AccessToken {
  def access_token: String
  def token_type: String
  def scope: Seq[String]
  def access_token_expiration: DateTime
  def refresh_token: String
  def refresh_token_expiration: DateTime
  def user_id: String
  def client_id: String
  def issued_on: DateTime
  def revoked: Boolean
  def revoked_on: Option[DateTime]

  def isAccessTokenExpired = access_token_expiration.isBefore(DateTime.now)
  def isRefreshTokenExpired = refresh_token_expiration.isBefore(DateTime.now)
  def isClientIDAuthorized = ClientIDService.findAuthorization(client_id, user_id).map{
    s => scope.diff(s.scope).isEmpty //scopes in token cannot be more than
    								 //authorized scopes
  }.getOrElse(false)

  def isValid = !isAccessTokenExpired && !revoked && isClientIDAuthorized
  
  lazy val expires_in = Minutes.minutesBetween(issued_on, access_token_expiration).getMinutes
}

trait AuthCode {
  def auth_code: String
  def user_id: String
  def client_id: String
  def redirect_uri: String
  def scope: Seq[String]
  def issued_on: DateTime
  def expire_on: DateTime
  def approval_prompt: Option[String]
  def state: Option[String]

  def isExpired = DateTime.now.isAfter(expire_on)
  
  def getScopes: Seq[Option[Scope]]
  def getClientID: Option[ClientID]
  
}

abstract class AccessTokenService(app: Application) extends BasePlugin with GuardbeeConfiguration {
  final val unique = true
  override def onStart() = {
    AccessTokenService.setService(this)
  }

  def getAccessToken(token: String): Option[AccessToken]
  def getAccessTokenByRefreshToken(refresh_token: String): Option[AccessToken]
  def saveAccessToken(access_token: AccessToken): Either[GuardbeeError, Unit]
  def deleteAccessToken(token: String): Either[GuardbeeError, Unit]
  def newAccessToken(
    access_token: String,
    token_type: String,
    scope: Seq[String],
    access_token_expiration: DateTime,
    refresh_token: String,
    refresh_token_expiration: DateTime,
    user_id: String,
    client_id: String,
    revoked: Boolean = false,
    issued_on: DateTime = DateTime.now,
    revoked_on: Option[DateTime] = None): AccessToken
  def revokeAccessToken(token: String): Either[GuardbeeError, Unit] = {
    getAccessToken(token).map {
      t =>
        saveAccessToken(newAccessToken(t.access_token,
          t.token_type,
          t.scope,
          t.access_token_expiration,
          t.refresh_token,
          t.refresh_token_expiration,
          t.user_id,
          t.client_id,
          true,
          t.issued_on,
          Some(DateTime.now)))
    }.getOrElse(Left(GuardbeeError.RevokeAccessTokenError))
  }

  def issueAccessToken(user_id: String, client_id: String, scope: Seq[String]): Either[GuardbeeError, AccessToken] = {
    val new_token = newAccessToken(TokenProvider.generate,
      "Bearer",
      scope,
      DateTime.now.plusSeconds(OAuth2AccessTokenExpiresIn),
      TokenProvider.generate,
      DateTime.now.plusSeconds(OAuth2RefreshTokenExpiresIn),
      user_id,
      client_id,
      false,
      DateTime.now,
      None)
    saveAccessToken(new_token) match {
      case Left(error) => Left(error)
      case Right(unit) => Right(new_token)
    }
  }

  //AuthCode
  def getAuthCode(code: String): Option[AuthCode]
  def consumeAuthCode(code: String): Either[GuardbeeError, AuthCode]
  def saveAuthCode(code: AuthCode): Either[GuardbeeError, Unit]

  def newAuthCode(
    auth_code: String,
    user_id: String,
    client_id: String,
    redirect_uri: String,
    scope: Seq[String],
    issued_on: DateTime,
    expire_on: DateTime,
    approval_prompt: Option[String],
    state: Option[String]): AuthCode

}

object AccessTokenService extends ServiceCompanion[AccessTokenService] with GuardbeeConfiguration {
  val serviceName = "AccessTokenService"
  lazy val default = ""
  val Unique = true

  def getAccessToken(token: String): Option[AccessToken] = {
    getDelegate.map(_.getAccessToken(token)).getOrElse {
      notInitialized
      None
    }
  }
  def getAccessTokenByRefreshToken(refresh_token: String): Option[AccessToken] = {
    getDelegate.map(_.getAccessTokenByRefreshToken(refresh_token)).getOrElse {
      notInitialized
      None
    }
  }
  def saveAccessToken(access_token: AccessToken): Either[GuardbeeError, Unit] = {
    getDelegate map (_.saveAccessToken(access_token)) getOrElse {
      notInitialized
      Left(GuardbeeError.InternalServerError)
    }
  }
  def deleteAccessToken(token: String): Either[GuardbeeError, Unit] = {
    getDelegate map (_.deleteAccessToken(token)) getOrElse {
      notInitialized
      Left(GuardbeeError.InternalServerError)
    }
  }
  def issueAccessToken(user_id: String, client_id: String, scope: Seq[String]): Either[GuardbeeError, AccessToken] = {
    getDelegate map (_.issueAccessToken(user_id, client_id, scope)) getOrElse {
      notInitialized
      Left(GuardbeeError.InternalServerError)
    }
  }

  def newAccessToken(
    access_token: String,
    token_type: String,
    scope: Seq[String],
    access_token_expiration: DateTime,
    refresh_token: String,
    refresh_token_expiration: DateTime,
    user_id: String,
    client_id: String,
    revoked: Boolean = false,
    issued_on: DateTime = DateTime.now,
    revoked_on: Option[DateTime] = None): AccessToken = {
    getDelegate map (_.newAccessToken(
      access_token,
      token_type,
      scope,
      access_token_expiration,
      refresh_token,
      refresh_token_expiration,
      user_id,
      client_id,
      revoked,
      issued_on,
      revoked_on)) getOrElse {
        notInitialized
        null
      }

  }

  //AuthCode
  def getAuthCode(code: String): Option[AuthCode] = {
    getDelegate.map(_.getAuthCode(code)).getOrElse {
      notInitialized
      None
    }
  }
  def consumeAuthCode(code: String): Either[GuardbeeError, AuthCode] = {
    getDelegate.map(_.consumeAuthCode(code)).getOrElse {
      notInitialized
      Left(GuardbeeError.InternalServerError)
    }
  }
  def saveAuthCode(code: AuthCode): Either[GuardbeeError, Unit] = {
    getDelegate.map(_.saveAuthCode(code)).getOrElse {
      notInitialized
      Left(GuardbeeError.InternalServerError)
    }
  }
  def newAuthCode(
    auth_code: String,
    user_id: String,
    client_id: String,
    redirect_uri: String,
    scope: Seq[String],
    issued_on: DateTime,
    expire_on: DateTime,
    approval_prompt: Option[String],
    state: Option[String]): AuthCode = {
    getDelegate.map(_.newAuthCode(auth_code, user_id, client_id, redirect_uri, scope, issued_on, expire_on, approval_prompt, state))
      .getOrElse {
        notInitialized
        null
      }
  }

}


