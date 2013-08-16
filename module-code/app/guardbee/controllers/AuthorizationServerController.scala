package guardbee.controllers


import play.api.mvc.Controller
import guardbee.authorization._


object AuthorizationServerController extends Controller with Secured {
  
  
  def code = authorized(authorization = isAuthenticated) { implicit request => user =>
  	Ok
  }

}