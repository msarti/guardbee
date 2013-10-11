import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appOrganization = "com.elogiclab"
  val appName         = "guardbee"
  val appVersion      = "0.1-SNAPSHOT"

  val appDependencies = Seq(
    // Select Play modules
    //jdbc,      // The JDBC connection pool and the play.api.db API
    //anorm,     // Scala RDBMS Library
    //javaJdbc,  // Java database API
    //javaEbean, // Java Ebean plugin
    //javaJpa,   // Java JPA plugin
    filters,   // A set of built-in filters
    javaCore,  // The core Java API
    cache,
  
    // WebJars pull in client-side web libraries
    "org.webjars" %% "webjars-play" % "2.2.0",
    "org.webjars" % "bootstrap" % "2.3.2",
    "org.webjars" % "font-awesome" % "3.2.1",
    "org.mindrot" % "jbcrypt" % "0.3m"
  
    // Add your own project dependencies in the form:
    // "group" % "artifact" % "version"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    scalaVersion := "2.10.1",
    organization := appOrganization
    // Add your own project settings here      
  )

}
