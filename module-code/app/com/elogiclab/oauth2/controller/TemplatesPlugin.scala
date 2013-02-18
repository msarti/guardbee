package com.elogiclab.oauth2.controller

import play.api.Plugin
import play.api.Logger
import play.api.mvc.{ RequestHeader, Request }
import play.api.templates.Html
import play.api.Application
import com.elogiclab.oauth2.views.html.authError
import play.api.Play.current

trait TemplatesPlugin extends Plugin {

  override def onStart() {
    Logger.info("[play2-oauth] loaded templates plugin: %s".format(getClass.getName))
  }

  def getAuthErrorPage[A](errorKey:String, errorMessage: String)(implicit request: Request[A]): Html

}

class DefaultTemplatesPlugin(application: Application) extends TemplatesPlugin {

  def getAuthErrorPage[A](errorKey:String, errorMessage: String)(implicit request: Request[A]): Html = authError(errorKey, errorMessage)

}

object TemplatesHelper {
  private def templateAPI(implicit app: Application): TemplatesPlugin = {
    app.plugin[TemplatesPlugin] match {
      case Some(plugin) => plugin
      case None => throw new Exception("There is no cache TemplatesPlugin registered. Make sure at least one TemplatesPlugin implementation is enabled.")
    }
  }
  
  def getAuthErrorPage[A](errorKey:String,errorMessage: String)(implicit request: Request[A]): Html = {
    templateAPI.getAuthErrorPage(errorKey, errorMessage)(request)
  }

}