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


import play.api.mvc.{Action, Controller}
import play.api.libs.json.Json
import play.api.Routes

case class Message(value: String)

object MessageController extends Controller {

  implicit val fooWrites = Json.writes[Message]

  def getMessage = Action {
    Ok(Json.toJson(Message("Hello from Scala")))
  }

  def javascriptRoutes = Action { implicit request =>
    Ok(Routes.javascriptRouter("jsRoutes")(routes.javascript.MessageController.getMessage)).as(JAVASCRIPT)
  }

}