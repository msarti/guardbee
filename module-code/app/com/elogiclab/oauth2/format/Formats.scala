package com.elogiclab.oauth2.format

import play.api.data.format.Formatter
import play.api.data.FormError
import com.elogiclab.oauth2.authz.core._

object Formats {
  
  implicit def clientIdFormat: Formatter[ClientIdentity] = new Formatter[ClientIdentity] {

    def bind(key: String, data: Map[String, String]) = {
       val param = data.get(key)
       param match {
         case None => Left(Seq(FormError(key, "error.required", Nil)))
         case _ => {
        	 val res = ClientIdService.findByClientId(param.get)
             res match {
        	   case None => Left(Seq(FormError(key, "oauth-play2.client_id.notvalid", Nil)))
        	   case _ => Right(res.get)
        	 }
         }
       }
    }

    def unbind(key: String, value: ClientIdentity) = Map(key -> value.clientId)
  }
  
  implicit def scopeFormat: Formatter[Scope] = new Formatter[Scope] {
   
    def bind(key: String, data: Map[String, String]) = {
       val param = data.get(key)
       param match {
         case None => Left(Seq(FormError(key, "error.required", Nil)))
         case _ => {
        	 val res = ScopeService.findByCode(param.get)
             res match {
        	   case None => Left(Seq(FormError(key, "oauth-play2.scope.notvalid", Nil)))
        	   case _ => Right(res.get)
        	 }
         }
       }
    }

    
    def unbind(key: String, value: Scope) = Map(key -> value.scope)
  }
  

}