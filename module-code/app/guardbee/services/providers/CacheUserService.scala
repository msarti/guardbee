package guardbee.services.providers

import play.api.Application
import guardbee.services.UserService
import guardbee.services.User
import guardbee.services.Password
import guardbee.services.Error
import org.joda.time.DateTime
import guardbee.services.Errors

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
  roles: Seq[String]
) extends User


class CacheUserService(app: Application) extends UserService(app)  with BaseCacheStore {

  val id = "CacheUserService"
  
  def getUserByID(user_id: String): Option[User] = {
    getItem[User, String]("users", user_id)
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
  def saveUser(user: User): Either[Error, Unit] = {
    saveItem("users", user, user.user_id)
    Right()
  }
  def disableUser(user_id: String): Either[Error, Unit] = {
    getItem[SimpleUser, String]("users", user_id) match {
      case Some(user) => {
        saveUser(user.copy(enabled=false))
      }
      case _ => Left(Errors.UserNotFoundError)
      
    }
  }
  def enableUser(user_id: String, password: Option[Password] = None): Either[Error, User] = {
    getItem[SimpleUser, String]("users", user_id) match {
      case Some(user) => {
        val new_user = user.copy(enabled=true, password = password)
        saveUser(new_user) fold({
          error => Left(error)
        }, {
          u =>
          Right(new_user)
        })
      }
      case _ => Left(Errors.UserNotFoundError)
    }
  }
  
    def newUser(user_id: String,
    email: String,
    created_on: DateTime,
    enabled: Boolean,
    full_name: Option[String],
    avatar_url: Option[String],
    bio: Option[String],
    home_page: Option[String]): User = {
      SimpleUser(user_id, email, created_on, enabled, full_name, avatar_url, bio, home_page, None, Nil)
    }

}