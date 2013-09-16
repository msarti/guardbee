package guardbee.utils

import play.api.data.format.Formatter
import play.api.data.FormError
import guardbee.services._

object Formatter {
  
  implicit def scopesFormat: Formatter[Seq[Scope]] = new Formatter[Seq[Scope]] {
    def bind(key: String, data: Map[String, String]) = {
      val x = data.get(key) map {
        s => s.split(" ") 
        .map(ClientIDService.findScope(_))
        .toSeq
      } getOrElse (Nil) 
      
      if(x.isEmpty || x.exists(_.isEmpty)) {
        Left(Seq(FormError(key, "guardbee.error.scopeInvalid", Nil)))
      } else {
        Right(x.map(_.get))
      }
    }
    
    def unbind(key: String, value: Seq[Scope]) = Map(key -> value.map(v => v.scope).mkString(" "))
    
  }

}