import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "demo"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "guardbee" %% "guardbee" % "master"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      resolvers ++= Seq(Resolver.url("sbt-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns),
      		    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/")
    )

}
