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

package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import guardbee.utils.RoutesHelper
import play.api.Logger
import guardbee.controllers.Secured
import guardbee.authorization._
import play.api.http.MimeTypes

object MainController extends Controller with Secured {

  def index() = Action { implicit request =>
    
    Ok(views.html.index(""))
  } 
  
  
  def myAuth = authorized(authorization = hasRole("ROLE_ADMIN")) { implicit request => user =>
    Ok("OK")
  }
  
  def oauth = authorized(provider = Some("OAuth2"), 
      responseType = MimeTypes.JSON,
      authorization = hasScope("get_profil", "ROLE_ADMIN")) { implicit request => user =>
    Ok(user.user.user_id)
  }
  
  
  
}