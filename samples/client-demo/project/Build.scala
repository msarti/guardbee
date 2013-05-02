import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "client-demo"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      // Add your project dependencies here,
        "net.databinder.dispatch" %% "dispatch-core" % "0.9.5"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here   
        //scalaVersion := "2.9.1"
    )

}
