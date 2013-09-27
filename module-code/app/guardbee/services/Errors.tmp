package guardbee.services

import play.api.mvc.Request
import play.api.i18n.Messages
import play.api.http.Status._
import play.api.http.MimeTypes
import play.api.mvc.Results
import play.api.mvc.Result
import play.api.libs.json.Json
import guardbee.utils.RoutesHelper
import play.api.data.FormError
import play.api.Logger

sealed trait Error {
  
  def messageKeys: Seq[(String, Option[Seq[Any]])]
  def status: Int
  def errorCode: String
  def messages[A](implicit request: Request[A]) = messageKeys.map(v => Messages(v._1, v._2.getOrElse(Nil):_*))
  
  def htmlMessages[A](implicit request: Request[A]) = {
    
    messages(request).zipWithIndex.map {
      case (p, i) => ("message"+i, p)
    }
  }
  
  def jsonMessages[A](implicit request: Request[A]) = {
    messages(request).map(s => Json.obj("message"->s))
  }
  
  
  def htmlPageResult[A](implicit request: Request[A]): Result = {
    Results.Redirect(RoutesHelper.errorPage(status)).flashing("errorCode"->errorCode)
    .flashing(htmlMessages:_*)
  }
  def jsonResult[A](implicit request: Request[A]): Result = Results.Status(status)(Json.obj("error" -> Json.obj("code"->errorCode, "messages"->jsonMessages)))

  final def toResult[A](responseType: String)(implicit request: Request[A]): Result = {
    Logger.info(request.acceptedTypes.mkString(" "))
    responseType match {
      case MimeTypes.HTML => htmlPageResult
      case MimeTypes.JSON => jsonResult
    }
  }

}

trait Errors {
  case class SimpleError(messageKeys:Seq[(String, Option[Seq[Any]])], status:Int, errorCode: String) extends Error
  
  
  def GenericError(formErrors:Seq[FormError], status:Int, errorCode: String):Error = {
    SimpleError(formErrors.map(f => (f.message, Some(Seq(f.key)))), status, errorCode)
  }
  
  
  private val InternalServerErrorKey = "guardbee.error.internalServerError"
  object InternalServerError extends Error {
    lazy val messageKeys = Seq((InternalServerErrorKey, None))
    val status = INTERNAL_SERVER_ERROR
    val errorCode = "internal_server_error"
  }

  
  
  private val SaveErrorKey = "guardbee.error.saveError"
  object SaveError extends Error {
    lazy val messageKeys = Seq((SaveErrorKey, None))
    val status = INTERNAL_SERVER_ERROR
    val errorCode = "save_error"
  }

  private val UserNotFoundErrorKey = "guardbee.error.userNotFoundError"
  object UserNotFoundError extends Error {
    lazy val messageKeys = Seq((UserNotFoundErrorKey, None))
    val status = BAD_REQUEST
    val errorCode = "user_not_found_error"
  }

  
  private val RevokeAccessTokenErrorKey = "guardbee.error.revokeAccessTokenError"
  object RevokeAccessTokenError extends Error {
    lazy val messageKeys = Seq((RevokeAccessTokenErrorKey, None))
    val status = BAD_REQUEST
    val errorCode = "revoke_token_error"
  }

  private val AuthenticationErrorKey = "guardbee.error.authenticationError"
  object AuthenticationError extends Error {
    lazy val messageKeys = Seq((AuthenticationErrorKey, None))
    val status = UNAUTHORIZED
    val errorCode = "authentication_error"
  }

  private val InvalidTokenErrorKey = "guardbee.error.invalidTokenError"
  object InvalidAuthTokenError extends Error {
    lazy val messageKeys = Seq((InvalidTokenErrorKey, None))
    val status = UNAUTHORIZED
    val errorCode = "invalid_token"
  }

  private val InvalidCredentialsErrorKey = "guardbee.error.invalidCredentialsError"
  object InvalidCredentialsError extends Error {
    lazy val messageKeys = Seq((InvalidCredentialsErrorKey, None))
    val status = UNAUTHORIZED
    val errorCode = "invalid_credentials"
  }
  
  
  private val AuthenticationRequiredErrorKey = "guardbee.error.AuthenticationRequiredError"
  object AuthenticationRequiredError extends Error {
    lazy val messageKeys = Seq((AuthenticationRequiredErrorKey, None))
    val status = UNAUTHORIZED
    val errorCode = "authentication_required_error"

    override def htmlPageResult[A](implicit request: Request[A]): Result = Results.Redirect(RoutesHelper.loginPage(request.uri))
      
  }

  private val AccessDeniedErrorKey = "guardbee.error.accessDeniedError"
  object AccessDeniedError extends Error {
    lazy val messageKeys = Seq((AccessDeniedErrorKey, None))
    val status = UNAUTHORIZED
    val errorCode = "access_denied_error"
  }

  private val InvalidAuthCodeErrorKey = "guardbee.error.InvalidAuthCodeError"
  object InvalidAuthCodeError extends Error {
    lazy val messageKeys = Seq((InvalidAuthCodeErrorKey, None))
    val status = UNAUTHORIZED
    val errorCode = "invalid_auth_code_error"
  }
}

object Errors extends Errors