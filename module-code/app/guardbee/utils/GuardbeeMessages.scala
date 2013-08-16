package guardbee.utils

import play.api.mvc.Request
import play.api.i18n.Messages

trait GuardbeeMessages {
  
  val applicationKey = "guardbee"

}

trait GuardbeeErrors extends GuardbeeMessages {
  
  val errorKey = applicationKey + ".error"
  
  def InvalidOAuth2Token[A](implicit request: Request[A]) = Messages(errorKey + ".invalidOAuth2Token")
  
  def MessageAuthenticationRequired[A](implicit request: Request[A]) = Messages(errorKey + ".authenticationRequired")
  
  def InvalidCredentials[A](implicit request: Request[A]) = Messages(errorKey + ".invalidCredentials")

  def MessageAccessDenied[A](implicit request: Request[A]) = Messages(errorKey + ".accessDenied")

}

object GuardbeeErrors extends GuardbeeErrors
