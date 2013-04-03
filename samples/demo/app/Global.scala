import play.api._
import com.elogiclab.guardbee.core._
import securesocial.core.UserService
import securesocial.core.SocialUser
import securesocial.core.UserId
import securesocial.core.AuthenticationMethod
import securesocial.core.PasswordInfo
import org.joda.time.DateTime
import com.elogiclab.guardbee.model.SimpleClientApplication
import com.elogiclab.guardbee.model.SimpleScope

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")
    
    ClientAppService.save(SimpleClientApplication("test_client_id", "secret", "test@example.com" ,"Application Name", Some("Description"), Seq("http://192.168.1.66:9001/code"), DateTime.now))
    
    ScopeService.save(SimpleScope("get_profile", "Read the user profile"))
    UserService.save(SocialUser(UserId("test@example.com", "userpass"), 
        "First name", "Last Name", "full name", Some("test@example.com"), None, 
        AuthenticationMethod("userPassword"), None, None, Some(PasswordInfo("bcrypt", "$2a$10$4fWy7qVyH.gcjSc95t4qGeorEshJTDpuyYAl2BlhgRhWji7UIXbSC"))
        ) )
  }

}