
// Note: This file is autogenerated by Builder.  Please do not modify!
// Full resolvers can be removed in sbt 0.13
fullResolvers <<= (fullResolvers, bootResolvers) map {
  case (rs, Some(boot)) if !(rs exists (_.name == "activator-local")) =>
    // Add just builder-local repo (as first checked)
    val localRepos = boot filter (_.name == "activator-local")
    localRepos ++ rs
  case (rs, _) => rs
}

// shim plugins are needed when plugins are not "UI aware"
// (we need an interface for the UI program rather than an interface
// for a person at a command line).
// In future plans, we want plugins to have a built-in ability to be
// remote-controlled by a UI and then we would drop the shims.
addSbtPlugin("com.typesafe.activator" % "sbt-shim-play" % "0.2.1")
