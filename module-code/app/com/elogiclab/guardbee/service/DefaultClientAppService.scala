package com.elogiclab.guardbee.service

import com.elogiclab.guardbee.core.ClientAppServicePlugin
import play.api.Application
import com.elogiclab.guardbee.core.ClientApplication
import com.elogiclab.guardbee.model.SimpleClientApplication

class DefaultClientAppService(application: Application) extends ClientAppServicePlugin(application) {

  def save(app: ClientApplication): ClientApplication = {
    SimpleClientApplication.create(app)
  }

  def findByClientId(client_id: String): Option[ClientApplication] = {
    SimpleClientApplication.findByClientId(client_id)
  }

  def delete(client_id: String): Unit = {
    SimpleClientApplication.delete(client_id)
  }

}