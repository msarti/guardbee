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

  lazy val OAuth2SpecialURI = Play.configuration.getString("guardbee.oauth2.specialURI")
  .getOrElse("urn:ietf:wg:oauth:2.0:oob")

  lazy val OAuth2AlwaysAllowRedirectToLocalhost = Play.configuration.getBoolean("guardbee.oauth2.alwaysAllowRedirectToLocalhost")
  .getOrElse(false)

}

object GuardbeeConfiguration extends GuardbeeConfiguration
