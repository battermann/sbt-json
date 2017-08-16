# sbt-json

A sbt plugin for generating Scala case class sources for deserialization of raw json e.g. from API responses.

sbt-json integrates very well with the [play-json library](https://github.com/playframework/play-json) as it can also generate play-json formats for implicit conversion of a `JsValue` to its Scala representation.

## Installation

For now the plugin has to be declared as an [external source dependency](http://www.scala-sbt.org/0.13/docs/Plugins.html#1d%29+Project+dependency).

### Edit `project/plugins.sbt`

    lazy val root = (project in file(".")).dependsOn(sbtPlugin)
    
    lazy val sbtPlugin = uri("git://github.com/battermann/sbt-json")

### Edit `build.sbt`

Edit the `build.sbt` file to enable the plugin and to generate case class sources whenever the compile task is executed:

    lazy val root = (project in file("."))
      .enablePlugins(SbtJsonPlugin)
      ...

Also add the `generateJsonModels` task to the `sourceGenerators` and the play-json library:

    lazy val root = (project in file("."))
      .enablePlugins(SbtJsonPlugin)
      .settings(
        libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.0",
        sourceGenerators in Compile += (generateJsonModels in Compile)
      )
      
## Settings

| name     | default | description |
| -------- | ------- | ----------- |
| jsonInterpreter | `interpretWithPlayJsonFormats`    | Specifies which interpreter to use. `interpret` and `interpretWithPlayJsonFormats` |
| includeJsValues     | `includeAll`    | Combinator that specifies which JSON values should be in-/excluded for analyzation. `exceptEmptyArrays` and `exceptNullValues`. Example: `includeAll.exceptEmptyArrays` |
| jsonSourcesDirectory  | `src/main/resources/json` | Path containing the JSON data to analyze. |
| jsonUrls  | `Nil` | List of urls that serve JSON data to be analyzed. |

## Example

If you want to analyze JSON data form `https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1&mkt=en-US` and ignore empty arrays, add the following lines to the `build.sbt` file:

    jsonUrls += "https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1&mkt=en-US"
    includeJsValues := includeAll.exceptEmptyArrays

## Tasks

| name     | description |
| -------- | ----------- |
| printJsonModels | Prints the generated case classes to the console. |
| generateJsonModels | Generates case classes. |
