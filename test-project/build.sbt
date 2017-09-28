enablePlugins(SbtJsonPlugin)
name := "test-project"
scalaVersion := "2.12.2"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.0"
// jsonUrls += "https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1&mkt=en-US"
jsValueFilter := allJsValues.exceptNullValues.exceptEmptyArrays
jsonInterpreter := plainCaseClasses.withPlayJsonFormats
jsonOptionals += OptionalField("fbpost", "Fbpost", "message")
packageName := "my.json.models"
