package guardbee.services.providers

import play.api.Application
import play.api.mvc.Results
import guardbee.services._
import play.api.mvc._
import views.html.defaultpages.badRequest
import guardbee.utils.GuardbeeMessages
import guardbee.utils.GuardbeeErrors

case class OAuth2Token(token_type:String, token:String) extends Credentials

class OAuth2AuthenticationProvider(application: Application)
  extends AuthenticationProvider(application)  {
  
  val id = "OAuth2"

  type CredentialsType = OAuth2Token

  def extractAuthToken(implicit request: Request[AnyContent]): Either[Error, AuthenticationToken] = {
    request.headers.get("Authentication") map {_.split(" ")} filter { _.length == 2} map { s =>
      val Array(x, y) = s
      Right(AuthenticationToken(name = y, credentials = Some(OAuth2Token(x, y)))) 
    } getOrElse {Left(InvalidAuthTokenError)}
  }
  def authenticate(authToken: AuthenticationToken)(implicit request: Request[AnyContent]): Either[Error, Authentication] = {
    
    (for(
        credentials <- authToken.credentials if credentials.token_type == "Bearer";
        token <- AccessTokenService.getAccessToken(credentials.token) if token.isValid;
        user <- UserService.getUserByID(token.user_id)) 
      yield (user, token)) match {
      case None => Left(InvalidCredentialsError)
      case Some((user, token)) => Right(Authentication(user.user_id, user, UserService.getUserGrants(user), id, Some(token.scope)))
    }
  }
  
  override def getAuthentication(implicit request: Request[AnyContent]):  Either[Error, Authentication] = {
    val result = for(authToken <- extractAuthToken.right;
    authentication <- authenticate(authToken).right
    )
      yield authentication
      
    result 
  }

  def onLoginSuccess(authentication: Authentication)(implicit request: Request[AnyContent]): Result = Results.BadRequest
  def onLoginFailure(implicit request: Request[AnyContent]): Result = Results.BadRequest

}