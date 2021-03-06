package guardbee.utils

import play.api.Play
import play.api.Play.current
import play.api.mvc.Call



object RoutesHelper {
  
  lazy val assetClazz = Play.current.classloader.loadClass("controllers.ReverseAssets")
  lazy val assetMethods = assetClazz.newInstance().asInstanceOf[{
    def at(file: String): Call
  }]
  def assetAt(file: String) = assetMethods.at(file)
  
  lazy val webJarAssetClazz = Play.current.classloader.loadClass("controllers.ReverseWebJarAssets")
  lazy val webJarAssetMethods = webJarAssetClazz.newInstance().asInstanceOf[{
    def at(file: String): Call
  }]
  def webJarAt(file: String) = webJarAssetMethods.at(file)

  
  lazy val loginLogoutControllerClazz = Play.current.classloader.loadClass("guardbee.controllers.ReverseLoginLogoutController")
  
  lazy val loginLogoutControllerMethods = loginLogoutControllerClazz.newInstance().asInstanceOf[{
    def loginPage(destPage: String): Call
    def doLogout() : Call
    def doLogin() : Call
  }]
  
  def loginPage(destPage: String): Call = loginLogoutControllerMethods.loginPage(destPage)
  def doLogin(): Call = loginLogoutControllerMethods.doLogin()

  lazy val errorPagesControllerClazz = Play.current.classloader.loadClass("guardbee.controllers.ReverseErrorPagesController")
  lazy val errorPagesControllerMethods = errorPagesControllerClazz.newInstance().asInstanceOf[{
    def errorPage(status: Int): Call
  }]
  def errorPage(status: Int) = errorPagesControllerMethods.errorPage(status)

    lazy val authorizationServerControllerClazz = Play.current.classloader.loadClass("guardbee.controllers.ReverseAuthorizationServerController")

  
  lazy val authorizationServerControllerMethods = authorizationServerControllerClazz.newInstance().asInstanceOf[{
    def approve(approve: Boolean): Call
  }]
  def approve(approve: Boolean) = authorizationServerControllerMethods.approve(approve)

}