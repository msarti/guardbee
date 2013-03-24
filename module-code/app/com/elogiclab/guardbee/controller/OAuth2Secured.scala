package com.elogiclab.guardbee.controller

import play.api.mvc.Request
import play.api.mvc.WrappedRequest
import play.api.mvc.AnyContent
import play.api.mvc.Result
import play.api.mvc.Action
import play.api.mvc.Request
import play.api.mvc.Controller
import play.api.mvc.BodyParser
import play.api.i18n.Messages

import play.api.mvc.PlainResult
import play.api.libs.json.Json
import com.elogiclab.guardbee.core._


case class OAuth2Request[A, U](user: U, request: Request[A]) extends WrappedRequest(request)

trait OAuth2Secured[U] extends Controller { 


case class OauthError(error: String, code: Int, message: String)
  
  private def LOGIN_REQUIRED[A](implicit request: Request[A]): PlainResult = {
    Forbidden( Json.toJson(Map("error" -> Messages("guardbee.error.require_authentication")))).as(JSON)
  }
  private def MALFORMED_HEADER[A](implicit request: Request[A]): PlainResult = {
    Forbidden( Json.toJson(Map("error" -> Messages("guardbee.error.malformed_header")))).as(JSON)
  }
  private def INVALID_TOKEN[A](implicit request: Request[A]): PlainResult = {
    Forbidden( Json.toJson(Map("error" -> Messages("guardbee.error.invalid_token")))).as(JSON)
  }
  private def INVALID_TOKEN_TYPE[A](implicit request: Request[A]): PlainResult = {
    Forbidden( Json.toJson(Map("error" -> Messages("guardbee.error.invalid_token_type")))).as(JSON)
  }
  private def UNAUTHORIZED[A](implicit request: Request[A]): PlainResult = {
    Forbidden( Json.toJson(Map("error" -> Messages("guardbee.error.unauthorized")))).as(JSON)
  }

  
  

  
  def withToken[A](scope: String, p: BodyParser[A])(f: OAuth2Request[A, U] => Result)  = Action(p) {
    implicit request =>
      {
    	var validation_result = for(
    	    token <- getToken(request).right;
    	    autorization <- getAutorization(token, scope).right;
    	    user <- getUser(token.user).right
    	) yield user 

    	validation_result match {
    	  case Left(error) => error
    	  case Right(user) => f(OAuth2Request(user, request))
    	} 
      }
  }
  
  
  def withToken(scope: String)(f: OAuth2Request[AnyContent, U] => Result) :Action[AnyContent] = 
		  withToken(scope, parse.anyContent)(f)

  def findUser(user_id: String): Option[U]

  private def validateToken[A](token_type:String, token:String)(implicit request: Request[A]): Either[PlainResult, AccessToken] = {
    AccessTokenService.findByToken(token) match {
      case None => Left(INVALID_TOKEN)
      case Some(access_token) => {
        if(access_token.token_type != token_type)
          Left(INVALID_TOKEN_TYPE)
        else if(access_token.isTokenExpired)
          Left(INVALID_TOKEN)
        else
          Right(access_token)
      }
    }
  }
  
  
  
  private def tokenFromHeader[A](implicit request: Request[A]): Either[PlainResult, AccessToken] = {
    { 
      val h = request.headers.get("Authorization").getOrElse("none").split(" ")
	  if(h.length == 2) 
	    Some((h.head, h.last))
	  else
	    None
    } match {
      case None => Left(LOGIN_REQUIRED)
      case Some((token_type, token)) => validateToken(token_type, token)
    }
  }
  
  
  def getToken[A](request: Request[A]) = {
	  tokenFromHeader(request)
  }
  
  
  def getUser[A](user_id:String)(implicit request: Request[A]):Either[PlainResult, U] = {
    findUser(user_id) match {
      case None => Left(INVALID_TOKEN)
      case Some(user) => Right(user)
    }
    
  }
  
  def getAutorization[A](access_token: AccessToken, scope: String)(implicit request: Request[A]):Either[PlainResult, UserAuthorization] = {
    var authorization = UserAuthorizationService.findByClientIdAndUser(access_token.client_id, access_token.user)
    authorization match {
      case None => Left(UNAUTHORIZED)
      case Some(auth) => {
        auth.scope.find(s => s.scope == scope) match {
          case None => Left(UNAUTHORIZED)
          case Some(sc) => Right(auth)
        }
      }
    }
    
  }
  
  
  

}