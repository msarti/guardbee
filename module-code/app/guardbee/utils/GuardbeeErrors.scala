package guardbee.utils

import play.api.mvc.Request
import play.api.http.MimeTypes
import play.api.mvc.Results
import play.api.libs.json.Json
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.http.Status
import guardbee.utils.i18n._
import play.api.mvc.Result

case class GuardbeeError(error_code: String, messages: Seq[GuardbeeMessage], status: Int) {
  def apply[A](mimeType: String)(implicit request: Request[A]) = {
    mimeType match {
      case MimeTypes.JSON => toJson
      case _ => toHtml
    }
  }
  private def toJson[A](implicit request: Request[A]) = {
    val m = messages.map {
      message =>
        Json.obj("message" -> message())
    }
    Results.Status(status)(Json.obj("error" -> Json.obj("code" -> error_code, "messages" -> m)))
  }

  protected def toHtml[A](implicit request: Request[A]) = {
    val m = messages.zipWithIndex.map {
      case (p, i) => ("message" + i, p())
    }
    Results.Redirect(RoutesHelper.errorPage(status)).flashing("errorCode"->error_code)
    .flashing(m:_*)
  }

}

object GuardbeeError {
  def apply[A](status: Int, error_code: String, formErrors: Seq[FormError]): GuardbeeError = {
    val m = formErrors.map({
      error =>
        GuardbeeMessages.Message(error.message, error.key)
    })
    GuardbeeError(error_code, m, status)
  }
  
  val InvalidAuthCodeError = GuardbeeError("", Seq(GuardbeeMessages.InvalidAuthCode), Status.BAD_REQUEST)
  val InternalServerError = GuardbeeError("", Seq(GuardbeeMessages.InternalServerError), Status.BAD_REQUEST)
  val RevokeAccessTokenError = GuardbeeError("", Seq(GuardbeeMessages.RevokeAccessTokenError), Status.BAD_REQUEST)
  object AuthenticationRequiredError extends GuardbeeError("", Seq(GuardbeeMessages.AuthenticationRequired), Status.UNAUTHORIZED) {
        override def toHtml[A](implicit request: Request[A]) = Results.Redirect(RoutesHelper.loginPage(request.uri))
  }
  val InvalidAuthTokenError = GuardbeeError("", Seq(GuardbeeMessages.InvalidOAuth2Token), Status.UNAUTHORIZED)
  val InvalidCredentialsError = GuardbeeError("", Seq(GuardbeeMessages.InvalidCredentials), Status.UNAUTHORIZED)
  val AuthenticationError = GuardbeeError("", Seq(GuardbeeMessages.AuthenticationError), Status.UNAUTHORIZED)
  val UserNotFoundError = GuardbeeError("", Seq(GuardbeeMessages.UserNotFound), Status.UNAUTHORIZED)
  val AccessDeniedError = GuardbeeError("", Seq(GuardbeeMessages.AccessDenied), Status.UNAUTHORIZED)
}
