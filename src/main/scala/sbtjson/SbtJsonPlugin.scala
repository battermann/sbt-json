package sbtjson

import java.io.File

import cats.implicits._
import json2caseclass.model.CaseClass._
import json2caseclass.model.Config
import json2caseclass.model.Types.Interpreter
import json2caseclass.model.Types._
import json2caseclass._
import json2caseclass.implementation.{CaseClassToStringInterpreter, NameTransformer, SchemaExtractor}
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin
import sbtjson.ErrorMessages._

import scala.io.Source

object SbtJsonPlugin extends AutoPlugin {
  override def requires = JvmPlugin

  private def generateCaseClassSourcesFromFiles(
    src: File,
    env: Config,
    optionals: Map[String, Seq[(ClassName, ClassFieldName)]],
    packageName: String): Either[SbtJsonFailure, List[(String, String)]] = {
    val sourceFiles = Option(src.list) getOrElse Array() filter (_ endsWith ".json")
    sourceFiles.toList.map { file =>
      val srcFile = src / file
      val filename = file.take(file lastIndexOf '.')
      val name = NameTransformer.normalizeName("Model".toSuffix)(filename).toRootTypeName
      val json = Source.fromFile(srcFile).getLines.mkString
      CaseClassGenerator.generate(env)(
        json.toJsonString,
        name, getOptionals(optionals, name, packageName))
        .map(generatedSource => (name, addHeaderAndPackage(generatedSource, name, packageName)))
        .leftMap(err => CaseClassSourceGenFailure(s"$name.json", err))
    }
      .sequence
  }

  private def generateCaseClassSourceFromUrls(
    urls: Seq[String],
    env: Config,
    optionals: Map[String, Seq[(ClassName, ClassFieldName)]],
    packageName: String): Either[SbtJsonFailure, List[(String, String)]] = {
    urls.toList.map { url =>
      Http.request(url)
        .flatMap { json =>
          val nameFromUrl = url.replaceFirst(".*\\/([^\\/\\.?]+).*", "$1")
          val name = NameTransformer.normalizeName("Model".toSuffix)(nameFromUrl).toRootTypeName
          CaseClassGenerator.generate(env)(
            json.toJsonString, name,
            getOptionals(optionals, name, packageName))
            .map(source => (name, addHeaderAndPackage(source, name, packageName)))
            .leftMap(err => CaseClassSourceGenFailure(url, err))
        }
    }
      .sequence
  }

  private def generateSourceFiles(
    src: File,
    dst: File,
    urls: Seq[String],
    env: Config,
    optionals: Map[String, Seq[(ClassName, ClassFieldName)]],
    packageName: String) = {
    for {
      fromFiles <- generateCaseClassSourcesFromFiles(src, env, optionals, packageName)
      fromUrls <- generateCaseClassSourceFromUrls(urls,env, optionals, packageName)
    } yield {
      val generatedSources = fromUrls ++ fromFiles
      if (generatedSources.nonEmpty) dst.mkdirs()
      generatedSources.map { case (fileName, generatedSource) =>
        val dstFile = packageName.split(".").foldLeft(dst) { case (acc, path) =>
          acc / path
        } / fileName.toLowerCase / (fileName.capitalize + ".scala")
        IO.write(dstFile, generatedSource)
        dstFile
      }
    }
  }

  private def addHeaderAndPackage(generatedSource: String, name: String, packageName: String): String = {
    s"""/** MACHINE-GENERATED CODE. DO NOT EDIT DIRECTLY */
       |package $packageName.${name.toLowerCase}
       |
       |$generatedSource
       |"""
      .stripMargin
  }

  private def getOptionals(optionals: Map[String, Seq[(ClassName, ClassFieldName)]], name: String, packageName: String) = {
    optionals.getOrElse(name.toLowerCase, Nil) ++ optionals.getOrElse(
      s"$packageName.${name.toLowerCase}",
      Nil)
  }

  object autoImport {
    lazy val printJsonModels: TaskKey[Unit] = TaskKey[Unit]("print-json-models", "Prints the generated JSON models.")
    lazy val generateJsonModels: TaskKey[Seq[File]] = TaskKey[Seq[File]](
      "generate-json-models",
      "Generates JSON model case classes.")
    lazy val jsonInterpreter: SettingKey[Interpreter] = SettingKey[Interpreter](
      "json-interpreter",
      "Specifies which interpreter to use. `interpret` and `interpretWithPlayJsonFormats`")
    lazy val jsValueFilter: SettingKey[JsValueFilter] = SettingKey[JsValueFilter](
      "include",
      "Combinator that specifies which JSON values should be in-/excluded for analyzation. `exceptEmptyArrays` and `exceptNullValues`. Example: `includeAll.exceptEmptyArrays`")
    lazy val jsonSourcesDirectory: SettingKey[File] = SettingKey[File](
      "json-source-directory",
      "Path containing the `.json` files to analyze.")
    lazy val jsonUrls: SettingKey[Seq[String]] = SettingKey[Seq[String]](
      "json-urls", "List of urls that serve JSON data to be analyzed.")
    lazy val jsonOptionals: SettingKey[Seq[OptionalField]] = SettingKey[Seq[OptionalField]](
      "json-optionals",
      "Specify which fields should be optional, e.g. `jsonOptionals := Seq(OptionalField(\"<package_name>\", \"<class_name>\", \"<field_name>\"))`")
    lazy val packageNameForJsonModels: SettingKey[String] = SettingKey[String](
      "package-name-for-json-models", "Package name for the generated case classes.")
    lazy val scalaSourceDir: SettingKey[File] = SettingKey[File]("scala-source-dir", "Path for generated case classes.")

    case class OptionalField(
      packageName: String,
      className: String,
      fieldName: String
    )

    val plainCaseClasses: Interpreter = CaseClassToStringInterpreter.plainCaseClasses

    implicit class InterpreterOptions(interpreter: Interpreter) {
      def withPlayJsonFormats = CaseClassToStringInterpreter.withPlayJsonFormats(interpreter)
    }

    val allJsValues: JsValueFilter = SchemaExtractor.allJsValues

    implicit class JsValueFilterOptions(jsValueFilter: JsValueFilter) {
      def exceptEmptyArrays = SchemaExtractor.exceptEmptyArrays(jsValueFilter)
      def exceptNullValues = SchemaExtractor.exceptNullValues(jsValueFilter)
    }
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    jsonSourcesDirectory := baseDirectory.value / "src" / "main" / "resources" / "json",
    jsonUrls := Nil,
    jsValueFilter := allJsValues,
    jsonInterpreter := plainCaseClasses.withPlayJsonFormats,
    jsonOptionals := Nil,
    packageNameForJsonModels := "jsonmodels",
    scalaSourceDir := sourceManaged.value / "compiled_json",
    printJsonModels := {

      val optionals = toOptionalsMap(jsonOptionals.value)
      val env = Config(
        jsValueFilter = jsValueFilter.value,
        interpreter = jsonInterpreter.value
      )

      val result = for {
        fromFiles <- generateCaseClassSourcesFromFiles(
          jsonSourcesDirectory.value,
          env,
          optionals,
          packageNameForJsonModels.value
        )
        fromUrls <- generateCaseClassSourceFromUrls(
          jsonUrls.value,
          env,
          optionals,
          packageNameForJsonModels.value
        )
      } yield fromFiles ++ fromUrls

      result.fold(err => streams.value.log.error(mkMessage(err)), _.foreach(s => streams.value.log.info(s._2)))
    },
    generateJsonModels := {

      val optionals = toOptionalsMap(jsonOptionals.value)
      val env = Config(
        jsValueFilter = jsValueFilter.value,
        interpreter = jsonInterpreter.value
      )

      generateSourceFiles(
        jsonSourcesDirectory.value,
        scalaSourceDir.value,
        jsonUrls.value,
        env,
        optionals,
        packageNameForJsonModels.value
      )
        .fold(
          err => throw new Exception(mkMessage(err)),
          files => files
        )
    },
    sourceGenerators in Compile += (generateJsonModels in Compile),
    managedSourceDirectories in Compile += (scalaSourceDir in Compile).value
  )

  private def toOptionalsMap(optionals: Seq[OptionalField]): Map[String, Seq[(ClassName, ClassFieldName)]] = {
    optionals
      .groupBy { case OptionalField(pkgName, _, _) => pkgName }
      .map { case (key, value) => (key.toLowerCase, value.map { case OptionalField(_, cName, fName) =>
        (cName.toClassName, fName.toClassFieldName)
      })
      }
  }
}

