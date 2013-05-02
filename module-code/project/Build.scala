import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "guardbee"
    val appVersion      = "master"

    val appDependencies = Seq(
      "securesocial" %% "securesocial" % "master-SNAPSHOT",
      "joda-time" % "joda-time" % "2.1",
      jdbc, anorm
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      
      resolvers ++= Seq(Resolver.url("sbt-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns),
      		    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/")
    )

}
