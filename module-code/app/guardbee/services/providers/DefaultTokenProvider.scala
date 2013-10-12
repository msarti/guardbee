package guardbee.services.providers

import play.api.Application
import guardbee.services.TokenProvider
import java.util.UUID

class DefaultTokenProvider(application: Application) extends TokenProvider(application) {
  val id = "defaultTokenProvider"
    
  def generate(): String = UUID.randomUUID.toString

}