package guardbee.services

import play.api.Application

abstract class TokenProvider(application: Application) extends BasePlugin {
  final val unique = false

  override def onStart() = {
    TokenProvider.setService(this)
  }
  
  def generate(): String

}

object TokenProvider extends ServiceCompanion[TokenProvider] {
  val serviceName = "tokenProvider"
  val default = ""
  val Unique = true
  
  def generate(): String = {
    getDelegate.map(_.generate).getOrElse {
      notInitialized
      null
    }
  }

}