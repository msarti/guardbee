package controllers

import play.api.mvc.{ Action, Controller }
import play.api.libs.ws.WS
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object Application extends Controller {
  def index = Action {
    Ok(views.html.index.render("Hello Play Framework"))
  }

  def login = Action {
    Redirect("http://localhost:9000/oauth2/auth", Map(
      "client_id" -> Seq("clientId"),
      "redirect_uri" -> Seq("http://localhost:9001/code"),
      "scope" -> Seq("getProfile"),
      "state" -> Seq("/")))

  }

  def code(code: Option[String], state: Option[String]) = Action.async {
    import ExecutionContext.Implicits.global
    val token_uri = "http://localhost:9000/oauth2/token"

    code match {
      case Some(c) =>
        WS.url(token_uri).withQueryString(
          "code" -> c,
          "client_id" -> "clientId",
          "client_secret" -> "secret",
          "redirect_uri" -> "http://localhost:9001/code",
          "grant_type" -> "authorization_code").get().map {
          response => 
            val access_token = response.json \ "access_token"
            Redirect(state.getOrElse("/"))
        }

      case None => Future(BadRequest("bad request"))
    }
    
    
    

  }

}