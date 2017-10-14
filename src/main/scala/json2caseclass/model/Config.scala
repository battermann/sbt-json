package json2caseclass.model

import json2caseclass.{CaseClassToStringInterpreter, SchemaExtractorOptions}
import json2caseclass.SchemaExtractorOptions.JsValueFilter
import json2caseclass.model.Interpreter.Interpreter
import json2caseclass.model.suffix.{Suffix, SuffixTag}
import shapeless.tag

case class Config(
  jsValueFilter: JsValueFilter = SchemaExtractorOptions.allJsValues,
  suffix: Suffix = tag[SuffixTag][String]("Model"),
  interpreter: Interpreter = CaseClassToStringInterpreter.plainCaseClasses
)
