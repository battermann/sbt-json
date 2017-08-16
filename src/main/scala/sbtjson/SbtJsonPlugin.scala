package sbtjson

import java.io.File

import cats.implicits._
import j2cgen.{CaseClassGenerator, CaseClassToStringInterpreter, SchemaExtractorOptions}
import j2cgen.SchemaExtractorOptions._
import ErrorMessages._
import j2cgen.models.json._
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

import scala.io.Source

object SbtJsonPlugin extends AutoPlugin {

  private def generateCaseClassSources(
    src: File,
    createPlayJsonFormats: Boolean,
    ignoreEmptyArrays: Boolean) = {
    val sourceFiles = Option(src.list) getOrElse Array() filter (_ endsWith ".json")
    sourceFiles.toList.map { file =>
      val srcFile = src / file
      val name = file.take(file lastIndexOf '.')
      val json = Source.fromFile(srcFile).getLines.mkString
      CaseClassGenerator.generate(include = include(ignoreEmptyArrays),
        interpreter = interpreter(createPlayJsonFormats))(json.toJsonString, name.capitalize.toRootTypeName)
        .map(source => (name, source))
        .leftMap(err => CaseClassSourceGenFailure(err))
    }
      .sequenceU
  }

  private def generateCaseClassSourceFromUrls(
    urls: Seq[String],
    createPlayJsonFormats: Boolean,
    ignoreEmptyArrays: Boolean) = {
    urls.toList.map { url =>
      Http.request(url)
        .flatMap { json =>
          val name = url.replaceFirst(".*\\/([^\\/\\.?]+).*", "$1")
          CaseClassGenerator.generate(include = include(ignoreEmptyArrays),
            interpreter = interpreter(createPlayJsonFormats))(json.toJsonString, name.capitalize.toRootTypeName)
            .map(source => (name, source))
            .leftMap(err => CaseClassSourceGenFailure(err))
        }
    }
      .sequenceU
  }

  private def include(ignoreEmptyArrays: Boolean) = {
    if (ignoreEmptyArrays) {
      SchemaExtractorOptions.exceptEmptyArrays(SchemaExtractorOptions.includeAll)
    } else {
      SchemaExtractorOptions.includeAll
    }
  }

  private def interpreter(createPlayJsonFormats: Boolean) = {
    val interpreter =
      if (createPlayJsonFormats) {
        CaseClassToStringInterpreter.interpretWithPlayJsonFormats _
      } else {
        CaseClassToStringInterpreter.interpret _
      }
    interpreter
  }

  private def generateSourceFiles(
    src: File,
    dst: File,
    urls: Seq[String],
    createPlayJsonFormats: Boolean,
    ignoreEmptyArrays: Boolean) = {
    for {
      fromFiles <- generateCaseClassSources(src, createPlayJsonFormats, ignoreEmptyArrays)
      fromUrls <- generateCaseClassSourceFromUrls(urls, createPlayJsonFormats, ignoreEmptyArrays)
    } yield {
      val sources = fromUrls ++ fromFiles
      if (sources.nonEmpty) dst.mkdirs()
      sources.map { case (fileName, source) =>
        val nameSpace = "models.json." + fileName.toLowerCase
        val completeSource =
          s"""/** MACHINE-GENERATED CODE. DO NOT EDIT DIRECTLY */
             |package $nameSpace
             |
           |$source
          """.stripMargin
        val dstFile = dst / "models" / "json" / fileName.toLowerCase / (fileName.capitalize + ".scala")
        IO.write(dstFile, completeSource)
        dstFile
      }
    }
  }

  // override def requires = JvmPlugin

  object autoImport {
    lazy val printJsonModels: TaskKey[Unit] = TaskKey[Unit]("print-json-models", "Prints the generated JSON models.")
    lazy val generateJsonModels: TaskKey[Unit] = TaskKey[Unit]("generate-json-models",
      "Generates JSON model case classes.")
    lazy val playJsonFormats: SettingKey[Boolean] = SettingKey[Boolean]("play-json-formats",
      "Specifies if play JSON formats should be created.")
    lazy val ignoreEmptyArrays: SettingKey[Boolean] = SettingKey[Boolean]("ignore-empty-arrays",
      "Specifies if empty arrays should be ignored.")
    lazy val jsonSourcesDirectory: SettingKey[File] = SettingKey[File]("source-directory",
      "Path containing .JSON files.")
    lazy val jsonUrls: SettingKey[Seq[String]] = SettingKey[Seq[String]]("urls", "Urls that serve JSON data.")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    playJsonFormats := true,
    jsonSourcesDirectory := baseDirectory.value / "src" / "main" / "resources" / "json",
    jsonUrls := Nil,
    ignoreEmptyArrays := false,
    printJsonModels := {

      val result = for {
        fromFiles <- generateCaseClassSources(jsonSourcesDirectory.value, playJsonFormats.value,
          ignoreEmptyArrays.value)
        fromUrls <- generateCaseClassSourceFromUrls(jsonUrls.value, playJsonFormats.value, ignoreEmptyArrays.value)
      } yield fromFiles ++ fromUrls

      result.fold(err => streams.value.log.error(mkMessage(err)), _.foreach(s => streams.value.log.info(s._2)))
    },
    generateJsonModels := {
      val genSourceDir = sourceManaged.value / "main" / "compiled_json"

      generateSourceFiles(
        jsonSourcesDirectory.value,
        genSourceDir,
        jsonUrls.value,
        playJsonFormats.value,
        ignoreEmptyArrays.value)
        .fold(err => streams.value.log.error(mkMessage(err)), _ => ())
    }
  )
}

