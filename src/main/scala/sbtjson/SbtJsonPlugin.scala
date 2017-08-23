package sbtjson

import java.io.File

import cats.implicits._
import j2cgen.SchemaExtractorOptions._
import j2cgen.models.CaseClass.{ClassFieldName, ClassName, _}
import j2cgen.models.Interpreter.Interpreter
import j2cgen.models.json._
import j2cgen.{CaseClassGenerator, CaseClassToStringInterpreter, SchemaExtractorOptions}
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin
import sbtjson.ErrorMessages._

import scala.io.Source

object SbtJsonPlugin extends AutoPlugin {
  override def requires = JvmPlugin


  private def generateCaseClassSourcesFromFiles(
    src: File,
    interpreter: Interpreter,
    include: Include,
    optionals: Map[String, Seq[(ClassName, ClassFieldName)]]) = {
    val sourceFiles = Option(src.list) getOrElse Array() filter (_ endsWith ".json")
    sourceFiles.toList.map { file =>
      val srcFile = src / file
      val name = file.take(file lastIndexOf '.')
      val json = Source.fromFile(srcFile).getLines.mkString
      CaseClassGenerator.generate(include = include, interpreter = interpreter)(json.toJsonString,
        name.capitalize.toRootTypeName, getOptionals(optionals, name))
        .map(generatedSource => (name, addHeaderAndPackage(generatedSource, name)))
        .leftMap(err => CaseClassSourceGenFailure(s"$name.json", err))
    }
      .sequenceU
  }

  private def generateCaseClassSourceFromUrls(
    urls: Seq[String],
    interpreter: Interpreter,
    include: Include,
    optionals: Map[String, Seq[(ClassName, ClassFieldName)]]) = {
    urls.toList.map { url =>
      Http.request(url)
        .flatMap { json =>
          val name = url.replaceFirst(".*\\/([^\\/\\.?]+).*", "$1")
          CaseClassGenerator.generate(include = include,
            interpreter = interpreter)(json.toJsonString, name.capitalize.toRootTypeName,
            getOptionals(optionals, name))
            .map(source => (name, addHeaderAndPackage(source, name)))
            .leftMap(err => CaseClassSourceGenFailure(url, err))
        }
    }
      .sequenceU
  }

  private def generateSourceFiles(
    src: File,
    dst: File,
    urls: Seq[String],
    interpreter: Interpreter,
    include: Include,
    optionals: Map[String, Seq[(ClassName, ClassFieldName)]]) = {
    for {
      fromFiles <- generateCaseClassSourcesFromFiles(src, interpreter, include, optionals)
      fromUrls <- generateCaseClassSourceFromUrls(urls, interpreter, include, optionals)
    } yield {
      val generatedSources = fromUrls ++ fromFiles
      if (generatedSources.nonEmpty) dst.mkdirs()
      generatedSources.map { case (fileName, generatedSource) =>
        val dstFile = dst / "models" / "json" / fileName.toLowerCase / (fileName.capitalize + ".scala")
        IO.write(dstFile, generatedSource)
        dstFile
      }
    }
  }

  private def addHeaderAndPackage(generatedSource: String, name: String): String = {
    val packageName = "models.json." + name.toLowerCase
    s"""/** MACHINE-GENERATED CODE. DO NOT EDIT DIRECTLY */
       |package $packageName
       |
             |$generatedSource
       |"""
      .stripMargin
  }

  private def getOptionals(optionals: Map[String, Seq[(ClassName, ClassFieldName)]], packageName: String) = {
    optionals.getOrElse(packageName.toLowerCase, Nil) ++ optionals.getOrElse(s"models.json.${packageName.toLowerCase}",
      Nil)
  }

  object autoImport {
    lazy val printJsonModels: TaskKey[Unit] = TaskKey[Unit]("print-json-models", "Prints the generated JSON models.")
    lazy val generateJsonModels: TaskKey[Seq[File]] = TaskKey[Seq[File]]("generate-json-models",
      "Generates JSON model case classes.")
    lazy val jsonInterpreter: SettingKey[Interpreter] = SettingKey[Interpreter]("json-interpreter",
      "Specifies which interpreter to use. `interpret` and `interpretWithPlayJsonFormats`")
    lazy val includeJsValues: SettingKey[Include] = SettingKey[Include]("include",
      "Combinator that specifies which JSON values should be in-/excluded for analyzation. `exceptEmptyArrays` and `exceptNullValues`. Example: `includeAll.exceptEmptyArrays`")
    lazy val jsonSourcesDirectory: SettingKey[File] = SettingKey[File]("json-source-directory",
      "Path containing the `.json` files to analyze.")
    lazy val jsonUrls: SettingKey[Seq[String]] = SettingKey[Seq[String]]("json-urls", "List of urls that serve JSON data to be analyzed.")
    lazy val jsonOptionals: SettingKey[Seq[(String, String, String)]] = SettingKey[Seq[(String, String, String)]](
      "json-optionals", "Specify which fields should be optional, e.g. `jsonOptionals := Seq((\"<package_name>\", \"<class_name>\", \"<field_name>\"))`")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    jsonSourcesDirectory := baseDirectory.value / "src" / "main" / "resources" / "json",
    jsonUrls := Nil,
    includeJsValues := SchemaExtractorOptions.includeAll,
    jsonInterpreter := CaseClassToStringInterpreter.interpretWithPlayJsonFormats,
    jsonOptionals := Nil,
    printJsonModels := {

      val optionals = toOptionalsMap(jsonOptionals.value)

      val result = for {
        fromFiles <- generateCaseClassSourcesFromFiles(
          jsonSourcesDirectory.value,
          jsonInterpreter.value,
          includeJsValues.value,
          optionals
        )
        fromUrls <- generateCaseClassSourceFromUrls(
          jsonUrls.value,
          jsonInterpreter.value,
          includeJsValues.value,
          optionals
        )
      } yield fromFiles ++ fromUrls

      result.fold(err => streams.value.log.error(mkMessage(err)), _.foreach(s => streams.value.log.info(s._2)))
    },
    generateJsonModels := {

      val optionals = toOptionalsMap(jsonOptionals.value)
      val genSourceDir = sourceManaged.value / "main" / "compiled_json"

      generateSourceFiles(
        jsonSourcesDirectory.value,
        genSourceDir,
        jsonUrls.value,
        jsonInterpreter.value,
        includeJsValues.value,
        optionals)
        .fold(
          err => throw new Exception(mkMessage(err)),
          files => files
        )
    },
    sourceGenerators in Compile += (generateJsonModels in Compile)
  )

  private def toOptionalsMap(optionals: Seq[(String, String, String)]) = {
    optionals
      .groupBy { case (pkgName, _, _) => pkgName }
      .map { case (key, value) => (key.toLowerCase, value.map { case (_, cName, fName) =>
        (cName.toClassName, fName.toClassFieldName)
      })
      }
  }
}

