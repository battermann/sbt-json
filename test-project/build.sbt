lazy val root = (project in file("."))
  .enablePlugins(SbtJsonPlugin)
  .settings(
    name := "test-project",
    scalaVersion := "2.12.2",
    libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.0",
    jsonUrls += "https://api.coindesk.com/v1/bpi/currentprice.json",
    sourceGenerators in Compile += (generateJsonModels in Compile)
  )
