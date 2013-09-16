import play.api.GlobalSettings
import play.api.Application
import play.api.Logger
import guardbee.services.UserService
import guardbee.services.PasswordProvider
import guardbee.services.ClientIDService
import org.joda.time.DateTime
import play.api.mvc.WithFilters
import play.filters.csrf._

object Global extends WithFilters(new CSRFFilter()) with GlobalSettings {

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
    ClientIDService.save(ClientIDService.newClientId("clientId", "Test application", "admin@example.org", None, Seq("urn:ietf:wg:oauth:2.0:oob"), true, "secret", DateTime.now))
    
    
  }
  
  

}