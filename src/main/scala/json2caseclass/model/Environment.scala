package json2caseclass.model

import json2caseclass.implementation.SchemaExtractor
import json2caseclass.model.Types.JsValueFilter

case class Environment(
  jsValueFilter: JsValueFilter = SchemaExtractor.allJsValues,
  nameTransformer: NameTransformer
)
