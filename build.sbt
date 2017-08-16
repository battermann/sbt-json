
lazy val commonSettings = Seq(
  version in ThisBuild := "0.1.0",
  organization in ThisBuild := "<INSERT YOUR ORG HERE>",
  scalaVersion := "2.10.6"
)

lazy val root = (project in file("."))
  .dependsOn(j2cgen)
  .settings(
    commonSettings,
    sbtPlugin := true,
    name := "sbt-json"
  )

lazy val j2cgen = project
  .settings(
    name := "j2cgen",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots")
    ),
    libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.2",
    libraryDependencies += compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1",
    libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.0",
    libraryDependencies += "org.typelevel" %% "cats" % "0.9.0"
  )