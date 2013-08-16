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

import guardbee.services.Authentication
import guardbee.services.AuthenticationProvider
import guardbee.services.Error
import play.api.http.MimeTypes
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.Controller
import play.api.mvc.Request
import play.api.mvc.Result

trait Secured extends Controller  {
  
  
  
  
  
  def authorized(provider: Option[String] = None, responseType: String = MimeTypes.HTML, authorization: Option[Authentication] => Either[Error, Unit])
  (action: => Request[AnyContent] => Authentication => Result) = Action {
    implicit request =>
    
      val authentication =
      provider.map(AuthenticationProvider.getAuthentication(_)).getOrElse(AuthenticationProvider.getAuthentication)
      
      authentication match {
        case Left(error) => error.toResult(responseType)
        case Right(auth) => {
          authorization(Some(auth)) match {
            case Right(unit) => action(request)(auth)
            case Left(error) => error.toResult(responseType)
          }
        }
      }
  }
  

}