package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import dispatch._
import play.api.libs.json.Json

case class Login(auth_uri:String, client_id:String, client_secret:String, token_uri: String, scope: String)

object Application extends Controller {
 
   val loginForm = Form[Login](
		  mapping(
		      "auth_uri" -> text,
		      "client_id" -> text,
		      "client_secret" -> text,
		      "token_uri"-> text,
		      "scope" -> text
		  )(Login.apply)(Login.unapply)
  )
  
  def index = Action {
     
     
    Ok(views.html.index(loginForm.fill(Login("http://localhost:9000/auth", "test_client_id", "secret", "http://localhost:9000/token", "get_profile"))))
     
    //Ok(views.html.index(loginForm.fill(Login("https://accounts.google.com/o/oauth2/auth", "249329231839.apps.googleusercontent.com", "J-rqgruL_AvEvLUmI8O-NytW", "https://accounts.google.com/o/oauth2/token", "https://www.googleapis.com/auth/userinfo.profile"))))
  }
  
  
  
 
  
  
  def login = Action { implicit request =>
    loginForm.bindFromRequest()(request).fold(
    		formWithErrors => Ok(""),
    		login => {
			    Redirect(login.auth_uri, 
			        Map(
			            "redirect_uri" -> Seq("http://192.168.1.66:9001/code"),
			            "client_id" -> Seq(login.client_id),
			            "response_type" -> Seq("code"),
			            "scope" -> Seq(login.scope),
			            "state" -> Seq("http://localhost:9001/protected"),
			            "access_type" -> Seq("offline")
			            )).withSession("client_id" -> login.client_id, "client_secret" -> login.client_secret, "token_uri" -> login.token_uri)
    		}
    )
    
  }
  
  def code(code:String, state:String) = Action { implicit request =>
    val client_id = session.get("client_id").getOrElse("none")
    val client_secret = session.get("client_secret").getOrElse("client_secret")
    val token_uri = session.get("token_uri").getOrElse("none")
    val svc = url(token_uri).POST << Map("code" -> code, 
        "client_id" -> client_id, "client_secret" -> client_secret, 
        "redirect_uri" -> "http://192.168.1.66:9001/code", "grant_type" -> "authorization_code")

    val result = Http(svc OK as.String)
    
    for (c <- result) {
    	val json = Json.parse(c)
    	print(json)
    }
    
    Ok
  }
  
}