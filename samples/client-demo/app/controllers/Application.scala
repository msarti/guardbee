package controllers

import play.api.mvc.{ Action, Controller }
import play.api.libs.ws.WS
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import play.api.Logger
import play.api.Play

object Application extends Controller {


  def index = Action.async { request =>
    import ExecutionContext.Implicits.global
    request.session.get("access_token").map { s =>
      val profile_uri = Play.current.configuration.getString("getProfile.uri").getOrElse("http://localhost:9000/api/getProfile")
      WS.url(profile_uri).withHeaders("Authentication" -> ("Bearer "+s))
      .get.map {
        response =>
        val m = Some(Map(("email" -> (response.json \ "email").as[String])))
        
        Ok(views.html.index.render(m))
      }
    }.getOrElse {
      Future(Ok(views.html.index.render(None)))
    }

  }

  def login = Action {
    Redirect(Play.current.configuration.getString("auth.uri").getOrElse("http://localhost:9000/oauth2/auth"), Map(
      "client_id" -> Seq("clientId"),
      "redirect_uri" -> Seq(Play.current.configuration.getString("redirect.uri").getOrElse("http://localhost:9001/code")),
      "scope" -> Seq("getProfile"),
      "state" -> Seq("/")))

  }

  def code(code: Option[String], state: Option[String]) = Action.async {
    import ExecutionContext.Implicits.global
    val token_uri = Play.current.configuration.getString("token.uri").getOrElse("http://localhost:9000/oauth2/token")

    code match {
      case Some(c) =>
        WS.url(token_uri).withQueryString(
          "code" -> c,
          "client_id" -> "clientId",
          "client_secret" -> "secret",
          "redirect_uri" -> Play.current.configuration.getString("redirect.uri").getOrElse("http://localhost:9001/code"),
          "grant_type" -> "authorization_code").get().map {
            response =>
              val access_token = response.json \ "access_token"
              Logger.info("token: " + access_token)
              Redirect(state.getOrElse("/")).withSession(("access_token", access_token.as[String]))
          }

      case None => Future(BadRequest("bad request"))
    }

  }

}