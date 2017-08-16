package j2cgen

import cats.implicits._
import j2cgen.SchemaExtractorOptions.{Include, RootTypeName}
import j2cgen.models.CaseClass.{ClassFieldName, ClassName}
import j2cgen.models.Interpreter.Interpreter
import j2cgen.models.caseClassSource.CaseClassSource
import j2cgen.models.json.JsonString
import j2cgen.models.suffix.{Suffix, SuffixTag}
import j2cgen.models.{CaseClass, CaseClassGenerationFailure, SchemaNameGenerator}
import shapeless.tag
import SchemaExtractorOptions._

object CaseClassGenerator {

  def generate(
    include: Include = SchemaExtractorOptions.includeAll,
    suffix: Suffix = tag[SuffixTag][String]("Model"),
    interpreter: Interpreter = CaseClassToStringInterpreter.interpret)
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
      sObjects <- SchemaToCaseClassConverter.convert(schema)
      renamed = CaseClassManipulator.rename(CaseClassNameGenerator.makeUnique, sObjects)
      withOptionals = CaseClassManipulator.addOptionals(renamed, optionals)
    } yield interpreter(withOptionals)
  }
}
