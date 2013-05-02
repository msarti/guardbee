package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import dispatch._
import play.api.libs.json.Json
import play.api.cache.Cache
import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.libs.ws.WS
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.duration._
import views.html.success
import views.html.defaultpages.badRequest
case class Login(auth_uri: String, client_id: String, client_secret: String, token_uri: String, scope: String)

object Application extends Controller {

  val loginForm = Form[Login](
    mapping(
      "auth_uri" -> text,
      "client_id" -> text,
      "client_secret" -> text,
      "token_uri" -> text,
      "scope" -> text)(Login.apply)(Login.unapply))

  def index = Action {

    Ok(views.html.index(loginForm.fill(Login("http://localhost:9000/auth", "test_client_id", "secret", "http://localhost:9000/token", "get_profile"))))
  }

  def login = Action { implicit request =>
    loginForm.bindFromRequest()(request).fold(
      formWithErrors => Ok(""),
      login => {

        Cache.set("client_id", login.client_id)
        Cache.set("client_secret", login.client_secret)
        Cache.set("token_uri", login.token_uri)

        Redirect(login.auth_uri,
          Map(
            "redirect_uri" -> Seq("http://192.168.1.65:9001/code"),
            "client_id" -> Seq(login.client_id),
            "response_type" -> Seq("code"),
            "scope" -> Seq(login.scope),
            "state" -> Seq("http://localhost:9001/success"),
            "access_type" -> Seq("offline")))
      })

  }

  def extractToken(result: String): Either[Throwable, String] = {
    val json = Json.parse(result)

    val seq = for {
      token <- json \\ "access_token"
    } yield token.as[String]
    seq.headOption.toRight(new NullPointerException)
  }

  def getEmail(access_token: String) {
    Async {
      WS.url("http://localhost:9000/getEmail").get.map(response => Ok)
    }
  }

  def code(code: String, state: String) = Action { implicit request =>
    val client_id = Cache.getAs[String]("client_id").getOrElse("none")
    val client_secret = Cache.getAs[String]("client_secret").getOrElse("client_secret")
    val token_uri = Cache.getAs[String]("token_uri").getOrElse("none")

    Async {
      WS.url(token_uri).post(Map(
        "code" -> Seq(code),
        "client_id" -> Seq(client_id),
        "client_secret" -> Seq(client_secret),
        "redirect_uri" -> Seq("http://192.168.1.65:9001/code"),
        "grant_type" -> Seq("authorization_code"))).map(response => {
        val access_token = response.json \ "access_token"

        Cache.set("access_token", access_token.as[String])

        Redirect(state)
      })

    }

  }
  def success = Action { implicit request =>

    val access_token = Cache.get("access_token")

    Async {
      WS.url("http://localhost:9000/getEmail").withHeaders(("Authorization", "Bearer " + access_token.getOrElse("none"))).get.
        map(result => result.status match {
          case 200 => Ok(views.html.success(result.json))
          case _ => InternalServerError(views.html.failure(result.json))

        })

    }
  }

}