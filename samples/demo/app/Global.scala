import play.api._
import com.elogiclab.oauth2.authz.core.ClientIdService
import models.ClientId
import com.elogiclab.oauth2.authz.core.ScopeService
import com.elogiclab.oauth2.authz.core.Scope
import models.AppScope
import securesocial.core.UserService
import securesocial.core.SocialUser
import securesocial.core.UserId
import securesocial.core.AuthenticationMethod
import securesocial.core.PasswordInfo

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")
    
    ClientIdService.save(ClientId("test_client_id", "secret", "Application Name", Some("Description"), Seq("http://localhost:9001/code")))
    ScopeService.save(AppScope("get_email"))
    UserService.save(SocialUser(UserId("test@example.com", "userpass"), 
        "First name", "Last Name", "full name", Some("test@example.com"), None, 
        AuthenticationMethod("userPassword"), None, None, Some(PasswordInfo("bcrypt", "$2a$10$4fWy7qVyH.gcjSc95t4qGeorEshJTDpuyYAl2BlhgRhWji7UIXbSC"))
        ) )
  }

}