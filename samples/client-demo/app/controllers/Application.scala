package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
  
  def login = Action {
    
    
    Redirect("http://localhost:9000/auth", 
        Map(
            "redirect_uri" -> Seq("http://localhost:9001/code"),
            "client_id" -> Seq("test_client_id"),
            "response_type" -> Seq("code"),
            "scope" -> Seq("get_email")
            ))
  }
  
}