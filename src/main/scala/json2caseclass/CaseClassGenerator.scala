package json2caseclass

import java.util.UUID

import cats.implicits._
import json2caseclass.implementation.CaseClassNameGenerator._
import json2caseclass.implementation._
import json2caseclass.model.CaseClass.{ClassFieldName, ClassName}
import json2caseclass.model.{?, CaseClassGenerationFailure, Config, Environment}
import json2caseclass.model.Types._

object CaseClassGenerator {

  def generate(conf: Config)(
      jsonString: JsonString,
      rootTypeName: RootTypeName,
      optionals: Seq[(ClassName, ClassFieldName)] = Nil): Either[CaseClassGenerationFailure, CaseClassSource] = {

    val generate = for {
      jsValue <- ? <~ JsonParser.parse(jsonString)
      schema <- SchemaExtractor.extractSchemaFromJsonRoot(rootTypeName, jsValue)
      caseClasses <- ? <~ SchemaToCaseClassConverter.convert(schema)
      renamed = CaseClassOperations.renameAmbiguous(makeUnique, caseClasses)
      withOptionals = CaseClassOperations.addOptionals(renamed, optionals)
      caseClassSource = conf.interpreter(withOptionals)
    } yield caseClassSource

    val env = Environment(
      conf.jsValueFilter,
      NameTransformer(conf.suffix)
    )

    generate.runA(env, UUID.fromString("e61fef28-b115-11e7-abc4-cec278b6b50a"))
  }
}
