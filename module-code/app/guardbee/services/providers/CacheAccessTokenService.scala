package guardbee.services.providers

import guardbee.services.AccessTokenService
import play.api.Application
import guardbee.services.AccessToken
import org.joda.time.DateTime
import guardbee.utils.GuardbeeError
import guardbee.services.AuthCode
import guardbee.services.Scope
import guardbee.services.ClientIDService
import guardbee.services.ClientID


case class SimpleAccessToken(
  access_token: String,
  token_type: String,
  scope: Seq[String],
  access_token_expiration: DateTime,
  refresh_token: String,
  refresh_token_expiration: DateTime,
  user_id: String,
  client_id: String,
  issued_on: DateTime,
  revoked: Boolean,
  revoked_on: Option[DateTime]) extends AccessToken

case class SimpleAuthCode(
  auth_code: String,
  user_id: String,
  client_id: String,
  redirect_uri: String,
  scope: Seq[String],
  issued_on: DateTime,
  expire_on: DateTime,
  approval_prompt: Option[String],
  state: Option[String]) extends AuthCode {
  
  lazy val getClientID: Option[ClientID] = ClientIDService.findById(client_id)
  lazy val getScopes: Seq[Option[Scope]] = scope.map(ClientIDService.findScope(_))
  
  
}

class CacheAccessTokenService(app: Application) extends AccessTokenService(app) with BaseCacheStore {

  val id = "CacheAccessTokenService"

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
    SimpleAccessToken(
      access_token,
      token_type,
      scope,
      access_token_expiration,
      refresh_token,
      refresh_token_expiration,
      user_id,
      client_id,
      issued_on,
      revoked,
      revoked_on)
  }

  def getAccessToken(token: String): Option[AccessToken] = {
    getItem[AccessToken, String]("accessTokens", token)
  }
  def saveAccessToken(access_token: AccessToken): Either[GuardbeeError, Unit] = {
    saveItem[AccessToken, String]("accessTokens", access_token, access_token.access_token)
    saveItem[AccessToken, String]("refreshTokens", access_token, access_token.refresh_token)
    Right()

  }
  def deleteAccessToken(token: String): Either[GuardbeeError, Unit] = {
    getAccessToken(token).map {
      t =>
      deleteItem[AccessToken, String]("accessTokens", t.access_token)
      deleteItem[AccessToken, String]("refreshTokens", t.refresh_token)
      
    }
    
    deleteItem[AccessToken, String]("accessTokens", token)
    Right()
  }

  //AuthCode
  def getAuthCode(code: String): Option[AuthCode] = {
    getItem[AuthCode, String]("authCodes", code)
  }
  def consumeAuthCode(code: String): Either[GuardbeeError, AuthCode] = {
    getAuthCode(code) match {
      case Some(value) => {
        deleteItem[AuthCode, String]("authCodes", code)
        Right(value)
      }
      case _ => Left(GuardbeeError.InvalidAuthCodeError)
    }

  }

  def saveAuthCode(code: AuthCode): Either[GuardbeeError, Unit] = {
    saveItem[AuthCode, String]("authCodes", code, code.auth_code)
    Right()
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
    SimpleAuthCode(
      auth_code,
      user_id,
      client_id,
      redirect_uri,
      scope,
      issued_on,
      expire_on,
      approval_prompt,
      state)
  }
  
  def getAccessTokenByRefreshToken(refresh_token: String): Option[AccessToken] = {
    getItem[AccessToken, String]("refreshTokens", refresh_token)
  }

}