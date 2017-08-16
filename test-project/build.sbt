import j2cgen.CaseClassToStringInterpreter
import j2cgen.SchemaExtractorOptions._

lazy val root = (project in file("."))
  .enablePlugins(SbtJsonPlugin)
  .settings(
    name := "test-project",
    scalaVersion := "2.12.2",
    libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.0",
    jsonUrls += "https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1&mkt=en-US",
    includeJsValues := includeAll.exceptEmptyArrays.exceptNullValues,
    jsonInterpreter := CaseClassToStringInterpreter.interpretWithPlayJsonFormats,
    sourceGenerators in Compile += (generateJsonModels in Compile)
  )
