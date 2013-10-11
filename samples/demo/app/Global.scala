
import play.api.GlobalSettings
import play.api.Logger
import guardbee.services._
import org.joda.time.DateTime
import play.api.Application

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
    ClientIDService.save(ClientIDService.newClientId("clientId", "TestApp", "Test application", "admin@example.org", None, Seq("urn:ietf:wg:oauth:2.0:oob"), true, "secret", DateTime.now))
    
    
  }
  


}