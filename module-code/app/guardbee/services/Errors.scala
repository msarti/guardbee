package guardbee.services

import play.api.mvc.Request
import play.api.i18n.Messages
import play.api.http.Status._
import play.api.http.MimeTypes
import play.api.mvc.Results
import play.api.mvc.Result
import play.api.libs.json.Json
import guardbee.utils.RoutesHelper

sealed trait Error {

  def messageKey: String
  def status: Int
  def errorCode: String

  def message[A](implicit request: Request[A]) = Messages(messageKey)
  
  def htmlPageResult[A](implicit request: Request[A]): Result = {
    Results.Redirect(RoutesHelper.errorPage(status)).flashing("errorCode"->errorCode, "message"->message)
  }
  def jsonResult[A](implicit request: Request[A]): Result = Results.Status(status)(Json.obj("error" -> Json.obj("code"->errorCode, "message"->message)))

  final def toResult[A](responseType: String)(implicit request: Request[A]): Result = {
    responseType match {
      case MimeTypes.HTML => htmlPageResult
      case MimeTypes.JSON => jsonResult
    }
  }

}

trait Errors {
  private val InternalServerErrorKey = "guardbee.error.internalServerError"
  object InternalServerError extends Error {
    lazy val messageKey = InternalServerErrorKey
    val status = INTERNAL_SERVER_ERROR
    val errorCode = "internal_server_error"
  }

  
  
  private val SaveErrorKey = "guardbee.error.saveError"
  object SaveError extends Error {
    lazy val messageKey = SaveErrorKey
    val status = INTERNAL_SERVER_ERROR
    val errorCode = "save_error"
  }

  private val UserNotFoundErrorKey = "guardbee.error.userNotFoundError"
  object UserNotFoundError extends Error {
    lazy val messageKey = UserNotFoundErrorKey
    val status = BAD_REQUEST
    val errorCode = "user_not_found_error"
  }

  
  private val RevokeAccessTokenErrorKey = "guardbee.error.revokeAccessTokenError"
  object RevokeAccessTokenError extends Error {
    lazy val messageKey = RevokeAccessTokenErrorKey
    val status = BAD_REQUEST
    val errorCode = "revoke_token_error"
  }

  private val AuthenticationErrorKey = "guardbee.error.authenticationError"
  object AuthenticationError extends Error {
    lazy val messageKey = AuthenticationErrorKey
    val status = UNAUTHORIZED
    val errorCode = "authentication_error"
  }

  private val InvalidTokenErrorKey = "guardbee.error.invalidTokenError"
  object InvalidAuthTokenError extends Error {
    lazy val messageKey = InvalidTokenErrorKey
    val status = UNAUTHORIZED
    val errorCode = "invalid_token"
  }

  private val InvalidCredentialsErrorKey = "guardbee.error.invalidCredentialsError"
  object InvalidCredentialsError extends Error {
    lazy val messageKey = InvalidCredentialsErrorKey
    val status = UNAUTHORIZED
    val errorCode = "invalid_credentials"
  }
  
  
  private val AuthenticationRequiredErrorKey = "guardbee.error.AuthenticationRequiredError"
  object AuthenticationRequiredError extends Error {
    lazy val messageKey = AuthenticationRequiredErrorKey
    val status = UNAUTHORIZED
    val errorCode = "authentication_required_error"

    override def htmlPageResult[A](implicit request: Request[A]): Result = Results.Redirect(RoutesHelper.loginPage(request.path))
      
  }

  private val AccessDeniedErrorKey = "guardbee.error.accessDeniedError"
  object AccessDeniedError extends Error {
    lazy val messageKey = AccessDeniedErrorKey
    val status = UNAUTHORIZED
    val errorCode = "access_denied_error"
  }

  private val InvalidAuthCodeErrorKey = "guardbee.error.InvalidAuthCodeError"
  object InvalidAuthCodeError extends Error {
    lazy val messageKey = InvalidAuthCodeErrorKey
    val status = UNAUTHORIZED
    val errorCode = "invalid_auth_code_error"
  }
}

object Errors extends Errors