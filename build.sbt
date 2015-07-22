name := "jarversion"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "com.github.scopt" %% "scopt" % "3.3.0"

libraryDependencies += "com.jsuereth" %% "scala-arm" % "1.4"

scalacOptions ++= Seq("-encoding", "UTF-8")

enablePlugins(JavaAppPackaging)