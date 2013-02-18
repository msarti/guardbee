package models

import com.elogiclab.oauth2.authz.core.{ClientIdentity, Scope}
import scala.collection.Seq

case class ClientId(clientId: String, clientSecret: String, redirectURIs:Seq[String]) extends ClientIdentity 

case class AppScope(scope: String) extends Scope
