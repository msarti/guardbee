import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "demo"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "play2-oauth" % "play2-oauth_2.9.1" % "1.0-SNAPSHOT"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
