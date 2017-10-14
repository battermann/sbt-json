package json2caseclass.model

import json2caseclass.SchemaExtractorOptions
import json2caseclass.SchemaExtractorOptions.JsValueFilter

case class Environment(
  jsValueFilter: JsValueFilter = SchemaExtractorOptions.allJsValues,
  nameGenerator: SchemaNameGenerator
)
