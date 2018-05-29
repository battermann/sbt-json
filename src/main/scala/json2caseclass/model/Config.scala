package json2caseclass.model

import json2caseclass.implementation.CaseClassToStringInterpreter._
import json2caseclass.implementation.SchemaExtractor
import json2caseclass.model.Types.{Interpreter, JsValueFilter, Suffix, SuffixTag}
import shapeless.tag

case class Config(
    jsValueFilter: JsValueFilter = SchemaExtractor.allJsValues,
    suffix: Suffix = tag[SuffixTag][String]("Model"),
    interpreter: Interpreter = plainCaseClasses
)
