package guardbee.services

import org.joda.time.DateTime

trait User {
  def user_id: String
  def email: String
  def created_on: DateTime
  def enabled: Boolean
  def full_name: Option[String]
  def avatar_url: Option[String]
  def bio: Option[String]
  def home_page: Option[String] 
}

trait Credentials

case class Password(hasher: String, password: String, salt: Option[String] = None)
