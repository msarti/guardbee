package guardbee.controllers

import play.api.mvc.Controller
import guardbee.authorization._
import play.api.data.Form
import play.api.data.Forms._
import guardbee.services.providers.SimpleAuthCode
import org.joda.time.DateTime
import guardbee.services._
import play.api.data.format.Formatter
import play.api.data.FormError
import play.api.Logger

object AuthorizationServerController extends Controller with Secured {

  implicit def scopesFormat: Formatter[Seq[String]] = new Formatter[Seq[String]] {
    def bind(key: String, data: Map[String, String]) = {
      data.get(key) map {
        s =>
          s.split(" ")
            .toSeq
      } getOrElse (Nil) match {
        case Nil => Left(Seq(FormError(key, "guardbee.error.scopeInvalid", Nil)))
        case value => Right(value)
        
      }
    }

    def unbind(key: String, value: Seq[String]) = Map(key -> value.mkString(" "))

  }
  

  def code = authorized(authorization = isAuthenticated) { implicit request =>
    auth =>

      val form = Form(
        mapping(
          "auth_code" -> ignored("123456"),
          "user_id" -> ignored(auth.user.user_id),
          "client_id"-> text.verifying("guardbee.error.clientIdInvalid", { s=> ClientIDService.findById(s).isDefined}),
          "redirect_uri" -> text,
          "scope" -> of[Seq[String]].verifying("guardbee.error.scopeInvalid", { s => 
           (! s.isEmpty) && (! s.map(ClientIDService.findScope(_)).exists(_.isEmpty))
          }),
          "issued_on" -> ignored(DateTime.now),
          "expire_on" -> ignored(DateTime.now),
          "approval_prompt" -> optional(text).verifying("guardbee.error.approvalPromptInvalid", { s=> Seq("auto", "force").contains(s.getOrElse("auto"))}),
          "state" -> optional(text))(SimpleAuthCode.apply)(SimpleAuthCode.unapply)
          .verifying("guardbee.error.redirectUriInvalid", {
            s => ClientIDService.findById(s.client_id).map(_.redirectURIs.contains(s.redirect_uri)) getOrElse(false) 
          })
      )
          
          
          
      form.bindFromRequest()(request).fold({
        errors =>
          Logger.info("error - "+errors.errors.head)
      }, {
        success =>
          Logger.info("Success - "+success)
          
      })

      Ok
  }

}