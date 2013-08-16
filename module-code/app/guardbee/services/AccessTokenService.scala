package guardbee.services

import play.api.Application
import org.joda.time.DateTime
import java.util.UUID
import guardbee.utils.GuardbeeConfiguration

trait AccessToken {
  def access_token: String
  def token_type: String
  def scope: Seq[String]
  def access_token_expiration: DateTime
  def refresh_token: String
  def refresh_token_expiration: DateTime
  def user_id: String
  def issued_on: DateTime
  def revoked: Boolean
  def revoked_on: Option[DateTime]

  def isAccessTokenExpired = access_token_expiration.isBefore(DateTime.now)
  def isRefreshTokenExpired = refresh_token_expiration.isBefore(DateTime.now)

  def isValid = !isAccessTokenExpired && !revoked
}

trait AuthCode {
  def auth_code: String
  def user_id: String
  def redirect_uri: String
  def scope: Seq[String]
  def issued_on: DateTime
  def expire_on: DateTime

  def isExpired = DateTime.now.isAfter(expire_on)
}

abstract class AccessTokenService(app: Application) extends BasePlugin with GuardbeeConfiguration {
  final val unique = true
  override def onStart() = {
    AccessTokenService.setService(this)
  }

  def getAccessToken(token: String): Option[AccessToken]
  def saveAccessToken(access_token: AccessToken): Either[Error, Unit]
  def deleteAccessToken(token: String): Either[Error, Unit]
  def newAccessToken(
    access_token: String,
    token_type: String,
    scope: Seq[String],
    access_token_expiration: DateTime,
    refresh_token: String,
    refresh_token_expiration: DateTime,
    user_id: String,
    revoked: Boolean = false,
    issued_on: DateTime = DateTime.now,
    revoked_on: Option[DateTime] = None): AccessToken
  def revokeAccessToken(token: String): Either[Error, Unit] = {
    getAccessToken(token).map {
      t =>
        saveAccessToken(newAccessToken(t.access_token,
          t.token_type,
          t.scope,
          t.access_token_expiration,
          t.refresh_token,
          t.refresh_token_expiration,
          t.user_id,
          true,
          t.issued_on,
          Some(DateTime.now)))
    }.getOrElse(Left(Errors.RevokeAccessTokenError))
  }

  def issueAccessToken(user_id: String, scope: Seq[String]): Either[Error, AccessToken] = {
    val new_token = newAccessToken(UUID.randomUUID.toString,
      "Bearer",
      scope,
      DateTime.now.plusSeconds(OAuth2AccessTokenExpiresIn),
      UUID.randomUUID.toString,
      DateTime.now.plusSeconds(OAuth2RefreshTokenExpiresIn),
      user_id,
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
  def consumeAuthCode(code: String): Either[Error, AuthCode]
  def saveAuthCode(code: AuthCode): Either[Error, Unit]

  def newAuthCode(
    auth_code: String,
    user_id: String,
    redirect_uri: String,
    scope: Seq[String],
    issued_on: DateTime,
    expire_on: DateTime): AuthCode

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
  def saveAccessToken(access_token: AccessToken): Either[Error, Unit] = {
    getDelegate map (_.saveAccessToken(access_token)) getOrElse {
      notInitialized
      Left(Errors.InternalServerError)
    }
  }
  def deleteAccessToken(token: String): Either[Error, Unit] = {
    getDelegate map (_.deleteAccessToken(token)) getOrElse {
      notInitialized
      Left(Errors.InternalServerError)
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
  def consumeAuthCode(code: String): Either[Error, AuthCode] = {
    getDelegate.map(_.consumeAuthCode(code)).getOrElse {
      notInitialized
      Left(Errors.InternalServerError)
    }
  }
  def saveAuthCode(code: AuthCode): Either[Error, Unit] = {
    getDelegate.map(_.saveAuthCode(code)).getOrElse {
      notInitialized
      Left(Errors.InternalServerError)
    }
  }
  def newAuthCode(
    auth_code: String,
    user_id: String,
    redirect_uri: String,
    scope: Seq[String],
    issued_on: DateTime,
    expire_on: DateTime): AuthCode = {
    getDelegate.map(_.newAuthCode(auth_code, user_id, redirect_uri, scope, issued_on, expire_on))
      .getOrElse {
        notInitialized
        null
      }
  }

}


