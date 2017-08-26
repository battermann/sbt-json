
lazy val root = (project in file("."))
  .settings(
    version in ThisBuild := "0.2.3",
    organization in ThisBuild := "com.github.battermann",
    scalaVersion := "2.10.6",
    sbtPlugin := true,
    name := "sbt-json",
    mavenPublishingSettings,
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

lazy val mavenPublishingSettings: Seq[Def.Setting[_]] = Seq(
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  licenses += ("MIT License", url("https://github.com/battermann/sbt-json/blob/master/LICENSE")),
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => true },
  homepage := Some(url("https://github.com/battermann/sbt-json")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/battermann/sbt-json"),
      "scm:git@github.com:battermann/sbt-json.git"
    )
  ),
  developers := List(
    Developer(id="battermann", name="Leif Battermann", email="leifbattermann@gmail.com", url=url("http://github.com/battermann"))
  )
)