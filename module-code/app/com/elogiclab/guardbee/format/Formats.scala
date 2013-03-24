package com.elogiclab.guardbee.format

import play.api.data.format.Formatter
import play.api.data.FormError
import com.elogiclab.guardbee.core._

object Formats {

  implicit def clientIdFormat: Formatter[ClientApplication] = new Formatter[ClientApplication] {

    def bind(key: String, data: Map[String, String]) = {
      val param = data.get(key)
      param match {
        case None => Left(Seq(FormError(key, "error.required", Nil)))
        case _ => {
          val res = ClientAppService.findByClientId(param.get)
          res match {
            case None => Left(Seq(FormError(key, "oauth-play2.client_id.notvalid", Nil)))
            case _ => Right(res.get)
          }
        }
      }
    }

    def unbind(key: String, value: ClientApplication) = Map(key -> value.client_id)
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

  implicit def scopesFormat: Formatter[Seq[Scope]] = new Formatter[Seq[Scope]] {

    def bind(key: String, data: Map[String, String]) = {
      var result: Seq[Scope] = Seq[Scope]()
      data.get(key) match {
        case None => Left(Seq(FormError(key, "error.required", Nil)))
        case Some(scopes_string) => {
          val t =
          scopes_string.split(" ")
          .map(v => ScopeService.findByCode(v))
          .filter(_.isDefined).map(v => v.get)
          .toSeq
          t.isEmpty match {
            case true => Left(Seq(FormError(key, "error.required", Nil)))
            case false => Right(t)
          } 
        }
      }
      
    }

    def unbind(key: String, value: Seq[Scope]) = Map(key -> value.map(v => v.scope).mkString(" "))
  }

  implicit def authCodeFormat: Formatter[AuthCode] = new Formatter[AuthCode] {

    def bind(key: String, data: Map[String, String]) = {
      val param = data.get(key)
      param match {
        case None => Left(Seq(FormError(key, "error.required", Nil)))
        case Some(code) => {
          val res = AuthCodeService.consume(code)
          res match {
            case None => Left(Seq(FormError(key, "guardbee.authcode.notvalid", Nil)))
            case Some(authCode) => Right(authCode)
          }
        }
      }
    }

    def unbind(key: String, value: AuthCode) = Map(key -> value.code)
  }
}