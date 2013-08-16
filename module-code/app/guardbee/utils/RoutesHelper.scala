package guardbee.utils

import play.api.Play
import play.api.Play.current
import play.api.mvc.Call



object RoutesHelper {
  
  lazy val loginLogoutControllerClazz = Play.current.classloader.loadClass("guardbee.controllers.ReverseLoginLogoutController")
  
  lazy val loginLogoutControllerMethods = loginLogoutControllerClazz.newInstance().asInstanceOf[{
    def loginPage(destPage: String): Call
    def doLogout
  }]
  
  def loginPage(destPage: String): Call = loginLogoutControllerMethods.loginPage(destPage)

  lazy val errorPagesControllerClazz = Play.current.classloader.loadClass("guardbee.controllers.ReverseErrorPagesController")
  lazy val errorPagesControllerMethods = errorPagesControllerClazz.newInstance().asInstanceOf[{
    def errorPage(status: Int): Call
  }]

  def errorPage(status: Int) = errorPagesControllerMethods.errorPage(status)


}