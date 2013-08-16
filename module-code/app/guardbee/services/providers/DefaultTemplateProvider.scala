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


package guardbee.services.providers

import play.api.templates.Html
import guardbee.services.TemplateProvider
import play.api.Application
import play.api.mvc.Flash

class DefaultTemplateProvider(app: Application) extends TemplateProvider(app) {
	val id = "defaultTemplates"
	
	def loginPage(implicit flash: Flash): Html = guardbee.views.html.login()
	
	def errorPage(title:String, message:String): Html = guardbee.views.html.error(title, message)
}