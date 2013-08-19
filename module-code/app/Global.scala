import play.api.GlobalSettings
import play.api.Application
import play.api.Logger
import guardbee.services.UserService
import guardbee.services.PasswordProvider
import guardbee.services.ClientIDService
import org.joda.time.DateTime

object Global extends GlobalSettings {

  override def onStart(app: Application): Unit = {
    Logger.info("Starting application")
    val user = UserService.createDisabledUser("admin@example.org", "admin@example.org").fold({
      e =>

    }, {
      u =>
        UserService.enableUser(u.user_id, Some(PasswordProvider.hash("password")))
    })

    Logger.info("Created user " + user)

    ClientIDService.saveScope(ClientIDService.newScope("getProfile", "guardbee.scope.getProfile"))
    ClientIDService.save(ClientIDService.newClientId("clientId", "Test application", "admin@example.org", None, Seq("http://localhost:9000/code"), "secret", DateTime.now))
    
    
  }
  
  

}