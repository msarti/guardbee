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
import guardbee.utils.RoutesHelper
import play.api.http.MimeTypes
import play.api.mvc.Result
import guardbee.services.providers.SimpleAuthCode
import guardbee.utils.GuardbeeConfiguration
import guardbee.views.html.show_code
import java.net.URI
import java.net.URL
import play.api.mvc.Action
import play.api.libs.json.Json
import play.api.mvc.Request
import guardbee.utils.GuardbeeError
import guardbee.utils.i18n.GuardbeeMessages
import play.api.mvc.AnyContent

object AuthorizationServerController extends Controller with Secured {

  val logger = Logger("guardbee")

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

  def generateToken = {
    TokenProvider.generate
  }

  def validateRedirectURI(redirect_uri: String, client_id: ClientID) = {

    def isLocalhost: Boolean = {
      try {
        val url = new URL(redirect_uri)
        logger.debug("url " + url.toString() + " testing if localhost")
        url.getHost == "localhost"
      } catch {
        case _: Throwable => false
      }
    }

    client_id.redirectURIs.contains(redirect_uri) match {
      case false => {
        GuardbeeConfiguration.OAuth2AlwaysAllowRedirectToLocalhost && client_id.allowRedirectToLocalhost && isLocalhost
      }
      case true => true
    }

  }

  def code = authorized(authorization = isAuthenticated) { implicit request =>
    auth =>
      logger.debug("Entering code")
      val form = Form(
        mapping(
          "auth_code" -> ignored(generateToken),
          "user_id" -> ignored(auth.user.user_id),
          "client_id" -> text.verifying("guardbee.error.clientIdInvalid", { s => ClientIDService.findById(s).isDefined }),
          "redirect_uri" -> text,
          "scope" -> of[Seq[String]].verifying("guardbee.error.scopeInvalid", { s =>
            (!s.isEmpty) && (!s.map(ClientIDService.findScope(_)).exists(_.isEmpty))
          }),
          "issued_on" -> ignored(DateTime.now),
          "expire_on" -> ignored(DateTime.now),
          "approval_prompt" -> optional(text).verifying("guardbee.error.approvalPromptInvalid", { s => Seq("auto", "force").contains(s.getOrElse("auto")) }),
          "state" -> optional(text))(SimpleAuthCode.apply)(SimpleAuthCode.unapply)
          .verifying("guardbee.error.redirectUriInvalid", {
            s =>
              ClientIDService.findById(s.client_id).map {
                c => validateRedirectURI(s.redirect_uri, c)
              }.getOrElse(false)
          }))

      form.bindFromRequest()(request).fold({
        errors =>
          val e = errors.errors
          Logger.info(e.mkString("-"))

          GuardbeeError(BAD_REQUEST, "", e)(MimeTypes.HTML)
      }, {
        success =>
          AccessTokenService.saveAuthCode(success)

          if (success.approval_prompt.getOrElse("auto") == "force" ||
            findApproval(success).isEmpty) {
            Ok(TemplateProvider.appApprovalPage("", success.getClientID, success.getScopes, Some(success)))
          } else {
            success.redirect_uri match {
              case GuardbeeConfiguration.OAuth2SpecialURI => Ok(show_code(success))
              case _ => Redirect(success.redirect_uri, Map("code" -> Seq(success.auth_code)) ++ success.state.map(v => Map("state" -> Seq(v))).getOrElse(Map.empty[String, Seq[String]]))
            }
          }
      })
  }

  private def findApproval(authCode: AuthCode): Option[ClientIDAuthorization] = {
    ClientIDService.findAuthorization(authCode.client_id, authCode.user_id).filter {
      authorized =>
        authCode.scope.forall {
          codeScope => authorized.scope.contains(codeScope)
        }
    }
  }

  def approveAuthCode(code: AuthCode): Result = {
    logger.debug("Client_id approved by user: " + code.client_id)
    val a = ClientIDService.newClientIdAuthorization(code.client_id, code.user_id, code.scope, DateTime.now)
    ClientIDService.saveAuthorization(a)
    code.redirect_uri match {
      case GuardbeeConfiguration.OAuth2SpecialURI => Ok(show_code(code))
      case _ => Redirect(code.redirect_uri, Map("code" -> Seq(code.auth_code)) ++ code.state.map(v => Map("state" -> Seq(v))).getOrElse(Map.empty[String, Seq[String]]))
    }
  }

  def approve(approve: Boolean) = authorized(authorization = isAuthenticated) { implicit request =>
    logger.debug("Entering approve")
    auth =>
      case class ApprovalForm(redirect_uri: String, client_id: String, code: Option[String]) {
        lazy val getClientId = ClientIDService.findById(client_id)
        lazy val getCode = AccessTokenService.getAuthCode(code.getOrElse("null"))
      }

      //First of all test if clientId and redirect_uri are valid
      val form = Form(mapping("redirect_uri" -> text,
        "client_id" -> text,
        "code" -> optional(text))(ApprovalForm.apply)(ApprovalForm.unapply)
        .verifying("guardbee.error.clientIdInvalid", {
          s => s.getClientId.isDefined
        })
        .verifying("guardbee.error.redirectUriInvalid", {
          s =>
            s.getCode.forall(x => x.redirect_uri == s.redirect_uri) match {
              case false => {
                logger.error("Provided redirect_uri does not match the previsous auth_code redirect_uri")
                false
              }
              case _ => true
            }
        }))
      form.bindFromRequest()(request).fold({
        errors =>
          val e = errors.errors
          logger.info(e.mkString("-"))
          GuardbeeError(BAD_REQUEST, "", e)(MimeTypes.HTML)
      }, {
        success =>
          //If client_id and redirect_uri are valid I continue with approval flow
          logger.info("OK")

          success.getCode.map(s => approveAuthCode(s)).getOrElse(Redirect(success.redirect_uri, Map("error" -> Seq("invalid_auth_code"))))
      })

  }

  case class TokenForm(code: String, client_id: String, client_secret: String, redirect_uri: String, grant_type: String) {
    lazy val getClientID = ClientIDService.findById(client_id)
    lazy val getCode = AccessTokenService.getAuthCode(code)
  }

  def token = Action { implicit req =>
    val form = Form(
      single("grant_type" -> text.verifying(GuardbeeMessages.InvalidGrantType(), { g => (g == "authorization_code" || g == "refresh_token") })))
    form.bindFromRequest()(req).fold({
      error =>
        val e = error.errors
        logger.info(e.map(f => f.message).mkString("-"))
        GuardbeeError(BAD_REQUEST, "", error.errors)(MimeTypes.JSON)
    }, {
      success =>
        success match {
          case "authorization_code" => get_token
          case "refresh_token" => refresh_token
        }
    })
  }

  def get_token[A](implicit request: Request[A]): Result = {
    val form = Form(
      mapping(
        "code" -> text,
        "client_id" -> text,
        "client_secret" -> text,
        "redirect_uri" -> text,
        "grant_type" -> text.verifying(GuardbeeMessages.InvalidGrantType(), { g => g == "authorization_code" }))(TokenForm.apply)(TokenForm.unapply)
        .verifying(GuardbeeMessages.InvalidAuthCodeKey, { f => f.getCode.isDefined })
        .verifying(GuardbeeMessages.InvalidClientIDKey, { f => f.getClientID.isDefined })
        .verifying(GuardbeeMessages.InvalidClientIDKey, { f =>
          f.getCode.map { c =>
            c.client_id == f.client_id
          }.getOrElse(false)
        })
        .verifying(GuardbeeMessages.InvalidSecret(), { f =>
          f.getClientID.map { c =>
            c.secret == f.client_secret
          }.getOrElse(false)
        })
        .verifying(GuardbeeMessages.InvalidRedirectUriKey, { f =>
          f.getCode.map { c =>
            c.redirect_uri == f.redirect_uri
          }.getOrElse(false)
        }))
    form.bindFromRequest()(request).fold({
      errors =>
        val e = errors.errors
        logger.info(e.map(f => f.message).mkString("-"))
        GuardbeeError(BAD_REQUEST, "", errors.errors)(MimeTypes.JSON)
    }, {
      success =>
        tokenByAuthCode(success.code)
    })
  }

  def tokenByAuthCode[A](code: String)(implicit request: Request[A]) = {
    AccessTokenService.consumeAuthCode(code).fold({ error =>
      error(MimeTypes.JSON)
    }, { authCode =>
      AccessTokenService.issueAccessToken(authCode.user_id, authCode.client_id, authCode.scope).fold({ error =>
        error(MimeTypes.JSON)
      }, { token =>
        Ok(Json.obj("access_token" -> token.access_token,
          "expires_in" -> token.expires_in,
          "token_type" -> token.token_type,
          "refresh_token" -> token.refresh_token))
      })
    })
  }

  case class RefreshTokenForm(client_id: String, client_secret: String, refresh_token: String, grant_type: String) {
    lazy val getClientID = ClientIDService.findById(client_id)
    lazy val getToken = AccessTokenService.getAccessTokenByRefreshToken(refresh_token)
  }

  def refresh_token[A](implicit request: Request[A]): Result = {
    val form = Form(
      mapping(
        "client_id" -> text,
        "client_secret" -> text,
        "refresh_token" -> text,
        "grant_type" -> text.verifying(GuardbeeMessages.InvalidGrantType(), { g => g == "refresh_token" }))(RefreshTokenForm.apply)(RefreshTokenForm.unapply)
        .verifying(GuardbeeMessages.InvalidClientIDKey, { f => f.getClientID.isDefined })
        .verifying(GuardbeeMessages.InvalidRefreshTokenKey, { f => f.getToken.isDefined })
        .verifying(GuardbeeMessages.InvalidSecretKey, { f =>
          f.getClientID.map { c =>
            c.secret == f.client_secret
          }.getOrElse(false)
        })
        .verifying(GuardbeeMessages.InvalidRefreshTokenKey, { f =>
          f.getToken.map { c =>
            !c.isRefreshTokenExpired
          }.getOrElse(false)
        }))
    form.bindFromRequest()(request).fold({
      errors =>
        val e = errors.errors
        logger.info(e.map(f => f.message).mkString("-"))
        GuardbeeError(BAD_REQUEST, "", errors.errors)(MimeTypes.JSON)
    }, {
      success =>
        success.getToken.map {
          t =>
            AccessTokenService.issueAccessToken(t.user_id, t.client_id, t.scope).fold({ error =>
              error(MimeTypes.JSON)
            }, { token =>
              Ok(Json.obj("access_token" -> token.access_token,
                "expires_in" -> token.expires_in,
                "token_type" -> token.token_type,
                "refresh_token" -> token.refresh_token))
            })
        }.getOrElse(GuardbeeError.InvalidRefreshTokenError(MimeTypes.JSON))

    })
  }

}