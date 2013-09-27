package guardbee.utils

import play.api.mvc.Request
import play.api.http.MimeTypes
import play.api.mvc.Results
import play.api.libs.json.Json
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.http.Status

case class GuardbeeError(error_code: String, messages: Seq[(String, Seq[Any])], status: Int) {
  def apply[A](mimeType: String)(implicit request: Request[A]) = {
    mimeType match {
      case MimeTypes.JSON => toJson
      case _ => toHtml
    }
  }
  private def toJson[A](implicit request: Request[A]) = {
    val m = messages.map {
      message =>
        Json.obj("message" -> Messages(message._1, message._2: _*))
    }
    Results.Status(status)(Json.obj("error" -> Json.obj("code" -> error_code, "messages" -> m)))
  }

  private def toHtml[A](implicit request: Request[A]) = {
    val m = messages.zipWithIndex.map {
      case (p, i) => ("message" + i, Messages(p._1, p._2: _*))
    }
    Results.Redirect(RoutesHelper.errorPage(status)).flashing("errorCode"->error_code)
    .flashing(m:_*)
  }

}

object GuardbeeError {
  def apply(status: Int, error_code: String, formErrors: Seq[FormError]): GuardbeeError = {
    val m = formErrors.map({
      error =>
        (error.message, Seq(error.key))
    })
    GuardbeeError(error_code, m, status)
  }
  
  val InvalidAuthCodeError = GuardbeeError("", Seq(("", Seq[Any]())), Status.BAD_REQUEST)
  val InternalServerError = GuardbeeError("", Seq(("", Seq[Any]())), Status.BAD_REQUEST)
  val RevokeAccessTokenError = GuardbeeError("", Seq(("", Seq[Any]())), Status.BAD_REQUEST)
  val AuthenticationRequiredError = GuardbeeError("", Seq(("", Seq[Any]())), Status.UNAUTHORIZED)
  val InvalidAuthTokenError = GuardbeeError("", Seq(("", Seq[Any]())), Status.UNAUTHORIZED)
  val InvalidCredentialsError = GuardbeeError("", Seq(("", Seq[Any]())), Status.UNAUTHORIZED)
  val AuthenticationError = GuardbeeError("", Seq(("", Seq[Any]())), Status.UNAUTHORIZED)
  val UserNotFoundError = GuardbeeError("", Seq(("", Seq[Any]())), Status.UNAUTHORIZED)
  val AccessDeniedError = GuardbeeError("", Seq(("", Seq[Any]())), Status.UNAUTHORIZED)
}
