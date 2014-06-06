// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Repository for sonata snapshots
resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("play" % "sbt-plugin" % "2.1.1")

addSbtPlugin("com.ketalo.play.plugins" % "emberjs" % "0.6.0-SNAPSHOT")