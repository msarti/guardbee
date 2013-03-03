import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "demo"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "guardbee" % "guardbee_2.9.1" % "master"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
