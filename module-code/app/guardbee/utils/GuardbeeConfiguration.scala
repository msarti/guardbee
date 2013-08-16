package guardbee.utils

import play.api.Play.current
import play.api.Play

trait GuardbeeConfiguration {
  
  lazy val DefaultGuardbeeService = Play.configuration.getString("guardbee.defaultService")
		  .getOrElse("defaultGuardbeeService")
		  

  lazy val OAuth2AccessTokenExpiresIn = Play.configuration.getInt("guardbee.oauth2.accessTokenExpiresIn")
		  .getOrElse(3600)

  lazy val OAuth2RefreshTokenExpiresIn = Play.configuration.getInt("guardbee.oauth2.refreshTokenExpiresIn")
		  .getOrElse(2592000)
		  
}

object GuardbeeConfiguration extends GuardbeeConfiguration
