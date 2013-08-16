/**
 * Copyright 2013 Marco Sarti - twitter: @marconesarti
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package guardbee.controllers
import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.data._
import play.api.data.Forms._
import guardbee.services.AuthenticationProvider
import play.api.Logger
import play.api.mvc.Request
import play.api.mvc.AnyContent
import guardbee.services.TemplateProvider


case class Login(username: String, password: String, remember_me: Boolean)

object LoginLogoutController extends Controller {

  val form = Form(
    mapping(
      "username" -> text,
      "password" -> text,
      "remember-me" -> checked("Remember me"))(Login.apply)(Login.unapply))

  def doLogin = Action { implicit request =>
    AuthenticationProvider.handleLogin("usernamepassword")
  }

  def doLogout = Action { implicit request =>
    AuthenticationProvider.handleLogout

  }
  
  def loginPage(destPage: String) = Action { implicit request =>
    
    Ok(TemplateProvider.loginPage).withSession("original-url" -> destPage)
    
  }

}