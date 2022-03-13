ThisBuild / scalaVersion        := "2.13.7"
ThisBuild / version             := "1.0.0"
ThisBuild / organization        := "stanford-aha"

val chiselVersion = "3.5.0"

lazy val root = (project in file("."))
    .settings(
        name := "axi2sram",
        libraryDependencies ++= Seq(
            "edu.berkeley.cs" %% "chisel3" % chiselVersion,
            "edu.berkeley.cs" %% "chiseltest" % "0.5.0" % "test",
            "com.github.scopt" %% "scopt" % "4.0.1"
        ),
        scalacOptions ++= Seq(
            "-language:reflectiveCalls",
            "-deprecation",
            "-feature",
            "-Xcheckinit",
            "-Ymacro-annotations"
        ),
        addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full)
    )
