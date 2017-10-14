package json2caseclass.model

import json2caseclass.CaseClassToStringInterpreter._
import json2caseclass.SchemaExtractorOptions.JsValueFilter
import json2caseclass.model.Types.{Interpreter, Suffix, SuffixTag}
import json2caseclass.SchemaExtractorOptions
import shapeless.tag

case class Config(
  jsValueFilter: JsValueFilter = SchemaExtractorOptions.allJsValues,
  suffix: Suffix = tag[SuffixTag][String]("Model"),
  interpreter: Interpreter = plainCaseClasses
)
