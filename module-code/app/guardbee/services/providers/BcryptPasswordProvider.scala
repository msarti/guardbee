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

import guardbee.services.PasswordProvider
import play.api.Application
import guardbee.services.Password
import org.mindrot.jbcrypt.BCrypt

class BcryptPasswordProvider(application: Application) extends PasswordProvider(application) {
  val id = "bcrypt"
  val confKey = "guardbee.bcrypt"
  lazy val log_rounds = application.configuration.getInt(confKey+".log_rounds").getOrElse(10)
    
  def hash(plainpassword: String): Password = {
    Password(id, BCrypt.hashpw(plainpassword, BCrypt.gensalt(log_rounds)))
  }

  def matches(candidate: String, password: Password): Boolean = {
    BCrypt.checkpw(candidate, password.password)
  }

}