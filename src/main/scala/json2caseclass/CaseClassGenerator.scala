package json2caseclass

import java.util.UUID

import cats.implicits._
import json2caseclass.CaseClassNameGenerator._
import json2caseclass.SchemaExtractorOptions.RootTypeName
import json2caseclass.model.CaseClass.{ClassFieldName, ClassName}
import json2caseclass.model._
import json2caseclass.model.caseClassSource.CaseClassSource
import json2caseclass.model.json.JsonString

object CaseClassGenerator {

  def generate(conf: Config)
    (
      jsonString: JsonString,
      rootTypeName: RootTypeName,
      optionals: Seq[(ClassName, ClassFieldName)] = Nil): Either[CaseClassGenerationFailure, CaseClassSource] = {

    val generate = for {
      jsValue <- ? <~ JsonParser.parse(jsonString)
      schema <- SchemaExtractor.extractSchemaFromJsonRoot(rootTypeName, jsValue)
      caseClasses <- ? <~ SchemaToCaseClassConverter.convert(schema)
      renamed = CaseClassOperations.rename(makeUnique, caseClasses)
      withOptionals = CaseClassOperations.addOptionals(renamed, optionals)
    } yield conf.interpreter(withOptionals)

    val env = Environment(
      jsValueFilter = conf.jsValueFilter,
      nameGenerator = SchemaNameGenerator(
        SchemaNameGeneratorImpl.generateCaseClassName(conf.suffix),
        SchemaNameGeneratorImpl.generateFieldName
      )
    )

    generate.runA(env, UUID.fromString("e61fef28-b115-11e7-abc4-cec278b6b50a"))
  }
}
