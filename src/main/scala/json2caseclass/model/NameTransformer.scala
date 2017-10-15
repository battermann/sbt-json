package json2caseclass.model

import json2caseclass.model.Schema.{SchemaFieldName, SchemaObjectName}

case class NameTransformer(
  makeSafeCamelCaseClassName: String => SchemaObjectName,
  makeSafeFieldName: String => SchemaFieldName
)
