package guardbee.services

import play.api.mvc.Request
import play.api.mvc.AnyContent
import play.api.mvc.Result
import play.api.mvc.Results
import guardbee.utils.GuardbeeError
import play.api.Application

abstract class PersistentAuthenticationProvider(application: Application) extends BasePlugin {
  type CredentialsType <: Credentials

  case class AuthenticationToken(
    name: String,
    credentials: Option[CredentialsType])

  def extractAuthToken(implicit request: Request[AnyContent]): Either[GuardbeeError, AuthenticationToken]
  def authenticate(authToken: AuthenticationToken)(implicit request: Request[AnyContent]): Either[GuardbeeError, Authentication]

  def onLoginSuccess(authentication: Authentication)(implicit request: Request[AnyContent]): Result
  def onLoginFailure(implicit request: Request[AnyContent]): Result

  def getAuthentication(implicit request: Request[AnyContent]): Either[GuardbeeError, Authentication] = {

    Authentication.getCookie flatMap {
      cookie =>
        AuthStoreProvider.get(cookie).filter(a => a.isExpired == false)
    } toRight (GuardbeeError.AuthenticationRequiredError)
  }

  def isAuthenticated(implicit request: Request[AnyContent]): Boolean = getAuthentication.isRight

  def handleAuthentication(implicit request: Request[AnyContent]): Either[GuardbeeError, Authentication] = {
    for (
      token <- extractAuthToken.right;
      auth <- authenticate(token).right
    ) yield auth
  }

  def commit(authentication: Authentication, onAuthorized: => Authentication => Result)(implicit request: Request[AnyContent]): Result = {
    val (cookie, value) = Authentication.createCookie
    AuthStoreProvider.save(value, authentication)
    onAuthorized(authentication).withCookies(cookie)
  }

  def handleLogout(implicit request: Request[AnyContent]): Result = {

    Authentication.getCookie match {
      case Some(value) => AuthStoreProvider.delete(value)
      case _ =>
    }
    Results.Redirect(Authentication.afterLogoutUrl).withCookies(Authentication.expireCookie)
  }

  def handleLogin(onAuthorized: => Authentication => Result, onUnauthorized: => Result)(implicit request: Request[AnyContent]): Result = {
    handleAuthentication match {
      case Left(error) => onUnauthorized
      case Right(success) => commit(success, onAuthorized)
    }
  }

  def handleLogin(implicit request: Request[AnyContent]): Result = {
    handleLogin(onLoginSuccess, onLoginFailure)
  }

  override def onStart() = {
    PersistentAuthenticationProvider.setService(this)
  }

}

object PersistentAuthenticationProvider extends ServiceCompanion[PersistentAuthenticationProvider] {

  val Unique = true;
  val serviceName = "persistentAuthenticationProvider";
  val default = "";
  
  def getAuthentication(implicit request: Request[AnyContent]): Either[GuardbeeError, Authentication] = {
    getDelegate.map(_.getAuthentication).getOrElse(Left(GuardbeeError.InternalServerError))
  }
  
  def handleLogin(implicit request: Request[AnyContent]): Result = {
    getDelegate.map(_.handleLogin).getOrElse({
      notInitialized
      Results.BadRequest
    })
  }
  
  def handleLogout(implicit request: Request[AnyContent]): Result = {
    getDelegate.map(_.handleLogout).getOrElse({
      notInitialized
      Results.BadRequest
    })
  }
  

}