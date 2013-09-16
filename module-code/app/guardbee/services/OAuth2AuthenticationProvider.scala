package guardbee.services

import play.api.mvc._

trait OAuth2Token {
  def tokenType: String
  def token: String
}

abstract class OAuth2AuthenticationProvider extends BasePlugin {

  type CredentialsType <: OAuth2Token

  case class AuthenticationToken(
    name: String,
    credentials: Option[CredentialsType])

  def extractAuthToken(implicit request: Request[AnyContent]): Either[Error, AuthenticationToken]
  def authenticate(authToken: AuthenticationToken)(implicit request: Request[AnyContent]): Either[Error, Authentication]

  override def onStart() = {
    OAuth2AuthenticationProvider.setService(this)
  }

}

object OAuth2AuthenticationProvider extends ServiceCompanion[OAuth2AuthenticationProvider] {
  val Unique = true;
  val serviceName = "OAuth2AuthenticationProvider";
  val default = "";

}