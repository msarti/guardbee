package guardbee.utils

import play.api.mvc.Request
import play.api.i18n.Messages
import play.api.data.FormError
import play.api.libs.json.Json
import play.api.mvc.Results
import play.api.http.MimeTypes
import play.api.data.FormError

trait GuardbeeMessagesBase {

  val applicationKey = "guardbee"

}

case class Message(key: String) {

  def apply[A]()(implicit request: Request[A]) = Messages(key)
  def apply[A, B](arg: B)(implicit request: Request[A]) = Messages(key, arg)
  def apply[A, B, C](arg1: B, arg2: C)(implicit request: Request[A]) = Messages(key, arg1, arg2)

}

trait GuardbeeErrorMessages extends GuardbeeMessagesBase {

  val errorKey = applicationKey + ".error"

  val InvalidOAuth2TokenKey = errorKey + ".invalidOAuth2Token"
  object InvalidOAuth2Token extends Message(InvalidOAuth2TokenKey)

  val MessageAuthenticationRequiredKey = errorKey + ".authenticationRequired"
  object MessageAuthenticationRequired extends Message(MessageAuthenticationRequiredKey)

  val InvalidCredentialsKey = errorKey + ".invalidCredentials"
  object InvalidCredentials extends Message(InvalidCredentialsKey)

  val MessageAccessDeniedKey = errorKey + ".accessDenied"
  object MessageAccessDenied extends Message(MessageAccessDeniedKey)

  val InvalidClientIDKey = errorKey + ".invalidClientID"
  object InvalidClientID extends Message(InvalidClientIDKey)

  val InvalidAuthCodeKey = errorKey + ".invalidAuthCode"
  object InvalidAuthCode extends Message(InvalidAuthCodeKey)

  val InvalidGrantTypeKey = errorKey + ".invalidGrantType"
  object InvalidGrantType extends Message(InvalidGrantTypeKey)

  val InvalidSecretKey = errorKey + ".invalidSecret"
  object InvalidSecret extends Message(InvalidSecretKey)

  val InvalidRedirectUriKey = errorKey + ".invalidRedirectUri"
  object InvalidRedirectUri extends Message(InvalidRedirectUriKey)

}


trait GuardbeeMessages extends GuardbeeMessagesBase {
  val messageKey = applicationKey + ".message"
  val AppApprovalKey = messageKey + ".appApproval"
  object AppApproval extends Message(AppApprovalKey)
}

object GuardbeeErrorMessages extends GuardbeeErrorMessages

object GuardbeeMessages extends GuardbeeMessages 
