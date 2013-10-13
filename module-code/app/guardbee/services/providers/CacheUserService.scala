package guardbee.services.providers

import play.api.Application
import guardbee.services.UserService
import guardbee.services.User
import guardbee.services.Password
import org.joda.time.DateTime
import guardbee.utils.GuardbeeError

case class SimpleUser(
  user_id: String,
  email: String,
  created_on: DateTime,
  enabled: Boolean,
  full_name: Option[String],
  avatar_url: Option[String],
  bio: Option[String],
  home_page: Option[String],
  password: Option[Password],
  roles: Seq[String]) extends User

class CacheUserService(app: Application) extends UserService(app) with BaseCacheStore {

  val id = "CacheUserService"

  def getUserByID(user_id: String): Option[SimpleUser] = {
    getItem[SimpleUser, String]("users", user_id)
  }
  def getUserPassword(user: User): Option[Password] = {
    getItem[SimpleUser, String]("users", user.user_id).map(_.password) getOrElse {
      None
    }
  }
  def getUserGrants(user: User): Seq[String] = {
    getItem[SimpleUser, String]("users", user.user_id).map(_.roles) getOrElse {
      Nil
    }
  }
  def saveUser(user: User): Either[GuardbeeError, Unit] = {
    saveItem("users", user, user.user_id)
    Right()
  }
  def disableUser(user_id: String): Either[GuardbeeError, Unit] = {
    getItem[SimpleUser, String]("users", user_id) match {
      case Some(user) => {
        saveUser(user.copy(enabled = false))
      }
      case _ => Left(GuardbeeError.UserNotFoundError)

    }
  }
  def enableUser(user_id: String, password: Option[Password] = None): Either[GuardbeeError, User] = {
    getItem[SimpleUser, String]("users", user_id) match {
      case Some(user) => {
        val new_user = user.copy(enabled = true, password = password)
        saveUser(new_user) fold ({
          error => Left(error)
        }, {
          u =>
            Right(new_user)
        })
      }
      case _ => Left(GuardbeeError.UserNotFoundError)
    }
  }

  override def newUser(user_id: String,
    email: String,
    created_on: DateTime,
    enabled: Boolean,
    full_name: Option[String],
    avatar_url: Option[String],
    bio: Option[String],
    home_page: Option[String],
    roles: Seq[String]): User = {
    SimpleUser(user_id, email, created_on, enabled, full_name, avatar_url, bio, home_page, None, roles)
  }

  
  override def setUserGrants(user_id: String, grants: Seq[String]): Either[GuardbeeError, Unit] = {
    getUserByID(user_id).map {
      user => saveUser(user.copy(roles = grants))
    }.getOrElse(Left(GuardbeeError.UserNotFoundError))
  }
}