/**
 * Copyright 2013 Marco Sarti - twitter: @marconesarti
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
import com.elogiclab.guardbee.auth.OauthError
import com.elogiclab.guardbee.auth.OauthError._
import play.api.Logger

/**
 * A request that adds the authenticated user for the current call
 */
case class OAuth2Request[A, U](user: U, request: Request[A]) extends WrappedRequest(request)

/**
 * @author marco
 *
 * Abstract controller that provides actions to be used to protect controllers.
 *
 */
trait OAuth2Secured[U] extends Controller with Oauth2Endpoint {


  def withScope[A](scope: String, p: BodyParser[A])(f: OAuth2Request[A, U] => Result) = Action(p) {
    implicit request =>
      {
        val validation_result = for (
          token <- validateToken(tokenExtractor).right;
          autorization <- getAutorization(token, scope).right;
          user <- getUser(token.user).right
        ) yield user

        validation_result match {
          case Left(error) => error.toJsonResponse
          case Right(user) => f(OAuth2Request(user, request))
        }
      }
  }

  /**
   * Secured action. If there is no access_token in request or the provided scope is not granted
   * by user it returns an error.
   *
   * @param scope the scope of this call
   * @param f the wrapped action to invoke
   * @return
   */
  def withScope(scope: String)(f: OAuth2Request[AnyContent, U] => Result): Action[AnyContent] =
    withScope(scope, parse.anyContent)(f)

  /**
   *
   * Implementation of this abstract method must return an instance of user of type U if and only if
   * the user exists and is active.
   *
   * @param user_id
   * @return
   */
  def findUser(user_id: String): Option[U]

  def tokenExtractor[A](request: Request[A]): Option[(String, String)] = {
    val h = request.headers.get("Authorization").getOrElse("none").split(" ")
    if (h.length == 2)
      Some((h.head, h.last))
    else
      None
  }

  def validateToken[A](extractor: Request[A] => Option[(String, String)])(implicit request: Request[A]): Either[OauthError, AccessToken] = {
    extractor(request) match {
      case None => Left(AUTHENTICATION_REQUIRED)
      case Some(("Bearer", access_token)) =>
        AccessTokenService.findByToken(access_token) match {
          case None => {
            Logger.debug("token '"+access_token+"' not found")
            Left(UNAUTHORIZED_ACCESS)
          }
          case Some(token) => if (token.isTokenExpired){
            Logger.debug("token '"+access_token+"' expired")
            Left(UNAUTHORIZED_ACCESS) 
          }
            else Right(token)
        }
      case _ => Left(INVALID_AUTHENTICATION_HEADER)
    }
  }

  /*
  private def validateToken[A](token_type: String, token: String)(implicit request: Request[A]): Either[PlainResult, AccessToken] = {
    AccessTokenService.findByToken(token) match {
      case None => Left(INVALID_TOKEN)
      case Some(access_token) => {
        if (access_token.token_type != token_type)
          Left(INVALID_TOKEN_TYPE)
        else if (access_token.isTokenExpired)
          Left(INVALID_TOKEN)
        else
          Right(access_token)
      }
    }
  }
*/



  def getUser[A](user_id: String)(implicit request: Request[A]): Either[OauthError, U] = {
    findUser(user_id) match {
      case None => Left(INVALID_TOKEN)
      case Some(user) => Right(user)
    }

  }

  def getAutorization[A](access_token: AccessToken, scope: String)(implicit request: Request[A]): Either[OauthError, UserGrant] = {
    val authorization = UserGrantService.findByClientIdAndUser(access_token.client_id, access_token.user)
    authorization match {
      case None => {
        Logger.debug("Application "+access_token.client_id+" not authorized by user "+access_token.user)
        Left(UNAUTHORIZED_ACCESS)
      }
      case Some(auth) => {
        auth.scope.find(s => s.scope == scope) match {
          case None => {
        	Logger.debug("Application "+access_token.client_id+" not authorized for scope '"+scope +"' ("+auth.scope+")")
            Left(UNAUTHORIZED_ACCESS)
          }
          case Some(sc) => Right(auth)
        }
      }
    }

  }

}