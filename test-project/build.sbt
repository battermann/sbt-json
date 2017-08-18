import j2cgen.CaseClassToStringInterpreter
import j2cgen.SchemaExtractorOptions._

enablePlugins(SbtJsonPlugin)
name := "test-project"
scalaVersion := "2.12.2"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.0"
jsonUrls += "https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1&mkt=en-US"
includeJsValues := includeAll.exceptEmptyArrays.exceptNullValues
jsonInterpreter := CaseClassToStringInterpreter.interpretWithPlayJsonFormats
jsonOptionals := Seq(("hpimagearchive", "Images", "url"))
