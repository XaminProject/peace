name := "peace"

version := "0.2.0"

scalaVersion := "2.9.2"

organization := "ir.xamin.peace"

libraryDependencies += "net.debasishg" %% "redisclient" % "2.7"

libraryDependencies += "net.debasishg" % "sjson_2.9.1" % "0.17"

libraryDependencies += "org.igniterealtime.smack" % "smack" % "3.2.1"

libraryDependencies += "org.igniterealtime.smack" % "smackx" % "3.2.1"

libraryDependencies += "org.clapper" %% "argot" % "0.4"

seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

libraryDependencies += "commons-lang" % "commons-lang" % "2.6"

libraryDependencies += "com.github.seratch" %% "scalikesolr" % "4.0.0"

