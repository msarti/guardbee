import play.api._
import com.elogiclab.oauth2.authz.core.ClientIdService
import models.ClientId
import com.elogiclab.oauth2.authz.core.ScopeService
import com.elogiclab.oauth2.authz.core.Scope
import models.AppScope

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")
    
    ClientIdService.save(ClientId("test_client_id", "secret", Seq("http://localhost:9001/code")))
    ScopeService.save(AppScope("get_email"))
    
  }

}