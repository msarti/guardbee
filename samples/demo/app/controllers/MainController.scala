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
import play.api.mvc.Action
import play.api.libs.ws.WS
import java.net.URLEncoder
import scala.concurrent.ExecutionContext
import guardbee.services.OAuth2AuthenticationProvider
import play.api.libs.json.Json
import play.filters.csrf.CSRFAddToken

object MainController extends Controller with Secured {

  def index() =
      Action { implicit request =>
        Ok(views.html.index(""))
    }

  def getProfile = authorized(provider = Some(OAuth2AuthenticationProvider.serviceName),
    authorization = hasScope("getProfile", "ROLE_USER"), responseType = MimeTypes.JSON) { implicit request =>
      authorization =>
        val user = authorization.user

        Ok(Json.obj("user_id" -> user.user_id,
          "avatar_url" -> user.avatar_url,
          "bio" -> user.bio,
          "email" -> user.email,
          "full_name" -> user.full_name,
          "home_page" -> user.home_page))
    }

}