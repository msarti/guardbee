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
import guardbee.services.TemplateProvider

/**
 * @author marco
 *
 */
object ErrorPagesController extends Controller {

  def errorPage(status: Int) = Action {
    implicit request =>
      val s = (0 to 20).toSeq.map(s => flash.get("message" + s)).collect {
        case Some(p) => p
      }

      Status(status)(TemplateProvider.errorPage("Error " + status, s))
  }

}