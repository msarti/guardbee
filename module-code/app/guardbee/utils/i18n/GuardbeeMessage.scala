package guardbee.utils.i18n

import play.api.i18n.Messages
import play.api.mvc.Request

case class GuardbeeMessage(key: String, args: Option[Seq[Any]]) {
  def apply[A]()(implicit request: Request[A]) = Messages(key, args.getOrElse(Nil): _*)
}

trait GuardbeeMessagesBase {
  def Message(key: String): GuardbeeMessage = GuardbeeMessage(key, None)
  def Message[A](key: String, arg: A): GuardbeeMessage = GuardbeeMessage(key, Some(Seq(arg)))
  def Message[A, B](key: String, arg1: A, arg2: B): GuardbeeMessage = GuardbeeMessage(key, Some(Seq(arg1, arg2)))

  val applicationKey = "guardbee"

}

trait GuardbeeMessages extends GuardbeeMessagesBase {

  val messageKey = applicationKey + ".message"
  val errorKey = applicationKey + ".error"
  val AppApprovalKey = messageKey + ".appApproval"
  val AppApproval = Message(AppApprovalKey, _:String)


  val InvalidOAuth2TokenKey = errorKey + ".invalidOAuth2Token"
  val InvalidOAuth2Token = Message(InvalidOAuth2TokenKey)

  val InvalidCredentialsKey = errorKey + ".invalidCredentials"
  val InvalidCredentials = Message(InvalidCredentialsKey)

  val AccessDeniedKey = errorKey + ".accessDenied"
  val AccessDenied = Message(AccessDeniedKey)

  val InvalidClientIDKey = errorKey + ".invalidClientID"
  val InvalidClientID = Message(InvalidClientIDKey)

  val InvalidAuthCodeKey = errorKey + ".invalidAuthCode"
  val InvalidAuthCode = Message(InvalidAuthCodeKey)

  val InvalidGrantTypeKey = errorKey + ".invalidGrantType"
  val InvalidGrantType = Message(InvalidGrantTypeKey)

  val InvalidSecretKey = errorKey + ".invalidSecret"
  val InvalidSecret = Message(InvalidSecretKey)

  val InvalidRedirectUriKey = errorKey + ".invalidRedirectUri"
  val InvalidRedirectUri = Message(InvalidRedirectUriKey)

  val InternalServerErrorKey = errorKey + ".internalServerError"
  val InternalServerError = Message(InternalServerErrorKey)

  val RevokeAccessTokenErrorKey = errorKey + ".revokeAccessTokenError"
  val RevokeAccessTokenError = Message(RevokeAccessTokenErrorKey)

  val AuthenticationRequiredKey = errorKey + ".authenticationRequired"
  val AuthenticationRequired = Message(AuthenticationRequiredKey)

  val AuthenticationErrorKey = errorKey + ".authenticationError"
  val AuthenticationError = Message(AuthenticationErrorKey)

  val UserNotFoundKey = errorKey + ".userNotFoundKey"
  val UserNotFound = Message(UserNotFoundKey)

}

object GuardbeeMessages extends GuardbeeMessages

