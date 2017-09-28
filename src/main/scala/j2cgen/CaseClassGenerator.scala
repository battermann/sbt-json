package j2cgen

import cats.implicits._
import j2cgen.SchemaExtractorOptions.{JsValueFilter, RootTypeName}
import j2cgen.models.CaseClass.{ClassFieldName, ClassName}
import j2cgen.models.Interpreter.Interpreter
import j2cgen.models.caseClassSource.CaseClassSource
import j2cgen.models.json.JsonString
import j2cgen.models.suffix.{Suffix, SuffixTag}
import j2cgen.models.{CaseClassGenerationFailure, SchemaNameGenerator}
import shapeless.tag

object CaseClassGenerator {

  def generate(
    include: JsValueFilter = SchemaExtractorOptions.allJsValues,
    suffix: Suffix = tag[SuffixTag][String]("Model"),
    interpreter: Interpreter = CaseClassToStringInterpreter.plainCaseClasses)
    (
      jsonString: JsonString,
      root: RootTypeName,
      optionals: Seq[(ClassName, ClassFieldName)] = Nil): Either[CaseClassGenerationFailure, CaseClassSource] = {

    val nameGenerator = SchemaNameGenerator(
      genClassName = SchemaNameGeneratorImpl.generateCaseClassName(suffix),
      genFieldName = SchemaNameGeneratorImpl.generateFieldName
    )

    for {
      jsValue <- JsonParser.parse(jsonString)
      schema <- SchemaExtractor.extractSchemaFromJsonRoot(include, nameGenerator, jsValue, root)
      caseClasses <- SchemaToCaseClassConverter.convert(schema)
      renamed = CaseClassManipulator.rename(CaseClassNameGenerator.makeUnique, caseClasses)
      withOptionals = CaseClassManipulator.addOptionals(renamed, optionals)
    } yield interpreter(withOptionals)
  }
}
