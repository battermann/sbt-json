package sbtjson

import java.io.File

import cats.implicits._
import j2cgen.{CaseClassGenerator, CaseClassToStringInterpreter, SchemaExtractorOptions}
import j2cgen.SchemaExtractorOptions._
import ErrorMessages._
import j2cgen.models.Interpreter.Interpreter
import j2cgen.models.json._
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

import scala.io.Source

object SbtJsonPlugin extends AutoPlugin {
  override def requires = JvmPlugin

  private def generateCaseClassSources(
    src: File,
    interpreter: Interpreter,
    include: Include) = {
    val sourceFiles = Option(src.list) getOrElse Array() filter (_ endsWith ".json")
    sourceFiles.toList.map { file =>
      val srcFile = src / file
      val name = file.take(file lastIndexOf '.')
      val json = Source.fromFile(srcFile).getLines.mkString
      CaseClassGenerator.generate(include = include, interpreter = interpreter)(json.toJsonString,
        name.capitalize.toRootTypeName)
        .map(source => (name, source))
        .leftMap(err => CaseClassSourceGenFailure(err))
    }
      .sequenceU
  }

  private def generateCaseClassSourceFromUrls(
    urls: Seq[String],
    interpreter: Interpreter,
    include: Include) = {
    urls.toList.map { url =>
      Http.request(url)
        .flatMap { json =>
          val name = url.replaceFirst(".*\\/([^\\/\\.?]+).*", "$1")
          CaseClassGenerator.generate(include = include,
            interpreter = interpreter)(json.toJsonString, name.capitalize.toRootTypeName)
            .map(source => (name, source))
            .leftMap(err => CaseClassSourceGenFailure(err))
        }
    }
      .sequenceU
  }

  private def generateSourceFiles(
    src: File,
    dst: File,
    urls: Seq[String],
    interpreter: Interpreter,
    include: Include) = {
    for {
      fromFiles <- generateCaseClassSources(src, interpreter, include)
      fromUrls <- generateCaseClassSourceFromUrls(urls, interpreter, include)
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

  object autoImport {
    lazy val printJsonModels: TaskKey[Unit] = TaskKey[Unit]("print-json-models", "Prints the generated JSON models.")
    lazy val generateJsonModels: TaskKey[Seq[File]] = TaskKey[Seq[File]]("generate-json-models",
      "Generates JSON model case classes.")
    lazy val jsonInterpreter: SettingKey[Interpreter] = SettingKey[Interpreter]("json-interpreter",
      "Specifies which interpreter to use to generate case class source.")
    lazy val includeJsValues: SettingKey[Include] = SettingKey[Include]("include",
      "Specifies if null values or empty arrays should be ignored.")
    lazy val jsonSourcesDirectory: SettingKey[File] = SettingKey[File]("json-source-directory",
      "Path containing .JSON files.")
    lazy val jsonUrls: SettingKey[Seq[String]] = SettingKey[Seq[String]]("json-urls", "Urls that serve JSON data.")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    jsonSourcesDirectory := baseDirectory.value / "src" / "main" / "resources" / "json",
    jsonUrls := Nil,
    includeJsValues := SchemaExtractorOptions.includeAll,
    jsonInterpreter := CaseClassToStringInterpreter.interpretWithPlayJsonFormats,
    printJsonModels := {

      val result = for {
        fromFiles <- generateCaseClassSources(jsonSourcesDirectory.value, jsonInterpreter.value, includeJsValues.value)
        fromUrls <- generateCaseClassSourceFromUrls(jsonUrls.value, jsonInterpreter.value, includeJsValues.value)
      } yield fromFiles ++ fromUrls

      result.fold(err => streams.value.log.error(mkMessage(err)), _.foreach(s => streams.value.log.info(s._2)))
    },
    generateJsonModels := {
      val genSourceDir = sourceManaged.value / "main" / "compiled_json"

      generateSourceFiles(
        jsonSourcesDirectory.value,
        genSourceDir,
        jsonUrls.value,
        jsonInterpreter.value,
        includeJsValues.value)
        .fold(
          err => throw new Exception(mkMessage(err)),
          files => files
        )
    }
  )
}

