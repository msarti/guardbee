package com.elogiclab.guardbee.auth

import com.elogiclab.guardbee.core.RoleService

class WithAdminRole extends UserAuthorization {
  import play.api.Play
  
  
  def adminRole: String = Play.current.configuration.getString("guardbee.admin.role").getOrElse("admin")

  def isAuthorized(user: UserAccount): Boolean = RoleService.getRoles(user).contains(adminRole)


}