package guardbee.services.providers

import play.api.Application
import guardbee.services.OAuth2AuthenticationProvider
import play.api.mvc._
import guardbee.utils.GuardbeeError
import guardbee.services._

class DefaultOAuth2AuthenticationProvider(application: Application) extends OAuth2AuthenticationProvider(application) {
  val id = "defaultOAuth2AuthenticationProvider"
  val unique = true

  case class DefaultOAuth2Token(token_type: String, token: String) extends OAuth2Token

  type CredentialsType = OAuth2Token

  def extractAuthToken(implicit request: Request[AnyContent]): Either[GuardbeeError, AuthenticationToken] = {
    request.headers.get("Authentication") map { _.split(" ") } filter { _.length == 2 } map { s =>
      val Array(x, y) = s
      Right(AuthenticationToken(name = y, credentials = Some(DefaultOAuth2Token(x, y))))
    } getOrElse { Left(GuardbeeError.InvalidAuthTokenError) }
  }
  def authenticate(authToken: AuthenticationToken)(implicit request: Request[AnyContent]): Either[GuardbeeError, Authentication] = {

    (for (
      credentials <- authToken.credentials if credentials.token_type == "Bearer";
      token <- AccessTokenService.getAccessToken(credentials.token) if token.isValid;
      user <- UserService.getUserByID(token.user_id) if user.enabled
    ) yield (user, token)) match {
      case None => Left(GuardbeeError.InvalidCredentialsError)
      case Some((user, token)) => Right(Authentication(user.user_id, user, UserService.getUserGrants(user), id, Some(token.scope)))
    }
  }

}