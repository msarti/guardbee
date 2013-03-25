package com.elogiclab.guardbee.auth.providers

import com.elogiclab.guardbee.controller.OAuth2Secured
import play.api.mvc.AnyContent
import securesocial.core.Identity
import securesocial.core.UserService
import securesocial.core.UserServicePlugin
import play.api.Logger

trait SecureSocialIdentity  {
  
  import play.api.Play._
  
  val plugin:UserServicePlugin = {
    application.plugin[UserServicePlugin] match {
      case None => {
        Logger.error("UserService was not initialized. Make sure a UserService plugin is specified in your play.plugins file")
        throw new RuntimeException("UserService not initialized")
      }
      case Some(plugin) => plugin
    }
  }
  
  
  def findUser(user_id: String): Option[Identity] = {
      plugin.findByEmailAndProvider(user_id, "userpass")
  }

}