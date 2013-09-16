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

package guardbee.services

import play.api.Application
import play.api.templates.Html
import play.api.mvc.Flash

abstract class TemplateProvider(app: Application) extends BasePlugin {
  final val unique = true

  override def onStart() = {
    TemplateProvider.setService(this)
  }
  
  def loginPage()(implicit flash: Flash,  token: play.filters.csrf.CSRF.Token): Html
  
  def errorPage(title:String, messages:Seq[String]): Html
  
  def appApprovalPage(title: String, client_id: Option[ClientID], scope: Seq[Option[Scope]], auth_code: Option[guardbee.services.AuthCode])(implicit token: play.filters.csrf.CSRF.Token): Html

}

object TemplateProvider extends ServiceCompanion[TemplateProvider] {
  val serviceName = "templateProvider"
  val default = "defaultTemplates"
  val Unique = true

  def loginPage()(implicit flash: Flash,  token: play.filters.csrf.CSRF.Token): Html = {
    this.getDelegate.map(_.loginPage()(flash, token)) getOrElse {
      this.notInitialized
      null
    }
  }
    
  def errorPage(title:String, messages:Seq[String]): Html = {
    this.getDelegate.map(_.errorPage(title, messages)) getOrElse {
      this.notInitialized
      null
    }
  }

   def appApprovalPage(title: String, client_id: Option[ClientID], scope: Seq[Option[guardbee.services.Scope]], auth_code: Option[guardbee.services.AuthCode])(implicit token: play.filters.csrf.CSRF.Token): Html = {
    this.getDelegate.map(_.appApprovalPage(title, client_id, scope, auth_code)) getOrElse {
      this.notInitialized
      null
    }
     
   }

}