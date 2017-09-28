package sbtjson

import java.io.File

import cats.implicits._
import j2cgen.SchemaExtractorOptions.{Include, _}
import j2cgen.{CaseClassGenerator, _}
import j2cgen.models.CaseClass._
import j2cgen.models.Interpreter.Interpreter
import j2cgen.models.json._
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
    optionals: Map[String, Seq[(ClassName, ClassFieldName)]],
    packageName: String) = {
    val sourceFiles = Option(src.list) getOrElse Array() filter (_ endsWith ".json")
    sourceFiles.toList.map { file =>
      val srcFile = src / file
      val name = file.take(file lastIndexOf '.')
      val json = Source.fromFile(srcFile).getLines.mkString
      CaseClassGenerator.generate(include = include, interpreter = interpreter)(
        json.toJsonString,
        name.capitalize.toRootTypeName, getOptionals(optionals, name, packageName))
        .map(generatedSource => (name, addHeaderAndPackage(generatedSource, name, packageName)))
        .leftMap(err => CaseClassSourceGenFailure(s"$name.json", err))
    }
      .sequenceU
  }

  private def generateCaseClassSourceFromUrls(
    urls: Seq[String],
    interpreter: Interpreter,
    include: Include,
    optionals: Map[String, Seq[(ClassName, ClassFieldName)]],
    packageName: String) = {
    urls.toList.map { url =>
      Http.request(url)
        .flatMap { json =>
          val name = url.replaceFirst(".*\\/([^\\/\\.?]+).*", "$1")
          CaseClassGenerator.generate(
            include = include,
            interpreter = interpreter)(
            json.toJsonString, name.capitalize.toRootTypeName,
            getOptionals(optionals, name, packageName))
            .map(source => (name, addHeaderAndPackage(source, name, packageName)))
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
    optionals: Map[String, Seq[(ClassName, ClassFieldName)]],
    packageName: String) = {
    for {
      fromFiles <- generateCaseClassSourcesFromFiles(src, interpreter, include, optionals, packageName)
      fromUrls <- generateCaseClassSourceFromUrls(urls, interpreter, include, optionals, packageName)
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
    lazy val includeJsValues: SettingKey[Include] = SettingKey[Include](
      "include",
      "Combinator that specifies which JSON values should be in-/excluded for analyzation. `exceptEmptyArrays` and `exceptNullValues`. Example: `includeAll.exceptEmptyArrays`")
    lazy val jsonSourcesDirectory: SettingKey[File] = SettingKey[File](
      "json-source-directory",
      "Path containing the `.json` files to analyze.")
    lazy val jsonUrls: SettingKey[Seq[String]] = SettingKey[Seq[String]](
      "json-urls", "List of urls that serve JSON data to be analyzed.")
    lazy val jsonOptionals: SettingKey[Seq[(String, String, String)]] = SettingKey[Seq[(String, String, String)]](
      "json-optionals",
      "Specify which fields should be optional, e.g. `jsonOptionals := Seq((\"<package_name>\", \"<class_name>\", \"<field_name>\"))`")
    lazy val packageName: SettingKey[String] = SettingKey[String](
      "package-name", "Package name for the generated case classes.")
    lazy val scalaSourceDir: SettingKey[File] = SettingKey[File]("scala-source-dir", "Path for generated case classes.")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    jsonSourcesDirectory := baseDirectory.value / "src" / "main" / "resources" / "json",
    jsonUrls := Nil,
    includeJsValues := SchemaExtractorOptions.includeAll,
    jsonInterpreter := CaseClassToStringInterpreter.interpretWithPlayJsonFormats,
    jsonOptionals := Nil,
    packageName := "jsonmodels",
    scalaSourceDir := sourceManaged.value / "compiled_json",
    printJsonModels := {

      val optionals = toOptionalsMap(jsonOptionals.value)

      val result = for {
        fromFiles <- generateCaseClassSourcesFromFiles(
          jsonSourcesDirectory.value,
          jsonInterpreter.value,
          includeJsValues.value,
          optionals,
          packageName.value
        )
        fromUrls <- generateCaseClassSourceFromUrls(
          jsonUrls.value,
          jsonInterpreter.value,
          includeJsValues.value,
          optionals,
          packageName.value
        )
      } yield fromFiles ++ fromUrls

      result.fold(err => streams.value.log.error(mkMessage(err)), _.foreach(s => streams.value.log.info(s._2)))
    },
    generateJsonModels := {

      val optionals = toOptionalsMap(jsonOptionals.value)

      generateSourceFiles(
        jsonSourcesDirectory.value,
        scalaSourceDir.value,
        jsonUrls.value,
        jsonInterpreter.value,
        includeJsValues.value,
        optionals,
        packageName.value
      )
        .fold(
          err => throw new Exception(mkMessage(err)),
          files => files
        )
    },
    sourceGenerators in Compile += (generateJsonModels in Compile),
    managedSourceDirectories in Compile += (scalaSourceDir in Compile).value
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

