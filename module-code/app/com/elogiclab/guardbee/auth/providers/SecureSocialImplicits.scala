package com.elogiclab.guardbee.auth.providers

import securesocial.core.Identity
import com.elogiclab.guardbee.auth.SimpleUserAccount

object SecureSocialImplicits {
  implicit def implicitDefs(identity: Identity) = new {
    def toUserAccount =
      SimpleUserAccount(username = identity.id.id, 
          firstName = identity.firstName, 
          lastName = identity.lastName, 
          avatarUrl = identity.avatarUrl)

  }

}