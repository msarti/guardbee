package guardbee.services

import guardbee.utils.GuardbeeConfiguration
import play.api.Application
import org.joda.time.DateTime
import guardbee.utils.GuardbeeError

abstract class UserService(app: Application) extends BasePlugin with GuardbeeConfiguration {

  final val unique = true
  
  def getUserByID(user_id: String): Option[User]
  def getUserPassword(user: User): Option[Password]
  def getUserGrants(user: User): Seq[String]
  def saveUser(user: User): Either[GuardbeeError, Unit]
  def disableUser(user_id: String): Either[GuardbeeError, Unit]
  def enableUser(user_id: String, password: Option[Password] = None): Either[GuardbeeError, User]

  def createDisabledUser(user_id: String, email: String): Either[GuardbeeError, User] = {
    val new_user = newUser(user_id, email, DateTime.now, false, None, None, None, None)
    saveUser(new_user) match {
      case Left(error) => Left(error)
      case Right(unit) => Right(new_user)
    }
  }

  def newUser(user_id: String,
    email: String,
    created_on: DateTime,
    enabled: Boolean,
    full_name: Option[String],
    avatar_url: Option[String],
    bio: Option[String],
    home_page: Option[String]): User

  override def onStart() = {
    UserService.setService(this)
  }

}

object UserService extends ServiceCompanion[UserService] with GuardbeeConfiguration {
  val serviceName = "UserService"
  lazy val default = "CacheUserService"
  val Unique = true

  def getUserByID(user_id: String): Option[User] = {
    getDelegate.map(_.getUserByID(user_id)) getOrElse {
      notInitialized
      None
    }
  }
  def getUserPassword(user: User): Option[Password] = {
    getDelegate.map(_.getUserPassword(user)) getOrElse {
      notInitialized
      None
    }
  }
  def getUserGrants(user: User): Seq[String] = {
    getDelegate.map(_.getUserGrants(user)) getOrElse {
      notInitialized
      Nil
    }
  }

  def saveUser(user: User): Either[GuardbeeError, Unit] = {
    getDelegate.map(_.saveUser(user)) getOrElse {
      notInitialized
      Left(GuardbeeError.InternalServerError)
    }
  }
  def disableUser(user_id: String): Either[GuardbeeError, Unit] = {
    getDelegate.map(_.disableUser(user_id)) getOrElse {
      notInitialized
      Left(GuardbeeError.InternalServerError)
    }
  }
  def enableUser(user_id: String, password: Option[Password] = None): Either[GuardbeeError, User] = {
    getDelegate.map(_.enableUser(user_id, password)) getOrElse {
      notInitialized
      Left(GuardbeeError.InternalServerError)
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

    getDelegate.map(
      _.newUser(user_id, email, created_on, enabled, full_name, avatar_url, bio, home_page)) getOrElse {
        notInitialized
        null
      }
  }

  
  def createDisabledUser(user_id: String, email: String): Either[GuardbeeError, User] = {
    val new_user = newUser(user_id, email, DateTime.now, false, None, None, None, None)
    saveUser(new_user) match {
      case Left(error) => Left(error)
      case Right(unit) => Right(new_user)
    }
  }

}