package guardbee.services

import play.api.mvc._
import guardbee.utils.GuardbeeError
import play.api.Application

trait OAuth2Token {
  def token_type: String
  def token: String
}

abstract class OAuth2AuthenticationProvider(app: Application) extends BasePlugin {

  type CredentialsType <: OAuth2Token

  case class AuthenticationToken(
    name: String,
    credentials: Option[CredentialsType])

  def extractAuthToken(implicit request: Request[AnyContent]): Either[GuardbeeError, AuthenticationToken]
  def authenticate(authToken: AuthenticationToken)(implicit request: Request[AnyContent]): Either[GuardbeeError, Authentication]

  def performAuthentication(implicit request: Request[AnyContent]):  Either[GuardbeeError, Authentication] = {
    val result = for(authToken <- extractAuthToken.right;
    authentication <- authenticate(authToken).right
    )
      yield authentication
      
    result 
  }

  
  override def onStart() = {
    OAuth2AuthenticationProvider.setService(this)
  }

}

object OAuth2AuthenticationProvider extends ServiceCompanion[OAuth2AuthenticationProvider] {
  val Unique = true;
  val serviceName = "OAuth2AuthenticationProvider";
  val default = "";

  def performAuthentication(implicit request: Request[AnyContent]): Either[GuardbeeError, Authentication] = {
    getDelegate.map(_.performAuthentication).getOrElse(Left(GuardbeeError.InternalServerError))
  }

}