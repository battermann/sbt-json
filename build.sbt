resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

lazy val root = (project in file("."))
  .settings(
    version in ThisBuild := "0.4.0",
    organization in ThisBuild := "com.github.battermann",
    crossSbtVersions := List("0.13.17", "1.1.5"),
    sbtPlugin := true,
    name := "sbt-json",
    mavenPublishingSettings,
    scalacOptions += "-feature",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots")
    ),
    libraryDependencies += compilerPlugin(
      "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1",
    libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.7",
    libraryDependencies += "org.typelevel" %% "cats-core" % "1.1.0",
    libraryDependencies += "com.lihaoyi" %% "pprint" % "0.5.3",
    libraryDependencies ++= Seq(
      "com.chuusai" %% "shapeless" % "2.3.3",
      compilerPlugin(
        "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    )
  )

lazy val mavenPublishingSettings: Seq[Def.Setting[_]] = Seq(
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  licenses += ("MIT License", url(
    "https://github.com/battermann/sbt-json/blob/master/LICENSE")),
  publishArtifact in Test := false,
  pomIncludeRepository := { _ =>
    true
  },
  homepage := Some(url("https://github.com/battermann/sbt-json")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/battermann/sbt-json"),
      "scm:git@github.com:battermann/sbt-json.git"
    )
  ),
  developers := List(
    Developer(id = "battermann",
              name = "Leif Battermann",
              email = "leifbattermann@gmail.com",
              url = url("http://github.com/battermann"))
  )
)
