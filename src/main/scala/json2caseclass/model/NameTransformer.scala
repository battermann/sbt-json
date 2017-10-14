package json2caseclass.model

import json2caseclass.model.Schema.{SchemaFieldName, SchemaObjectName}

case class NameTransformer(
  transformClassName: String => SchemaObjectName,
  transformFieldName: String => SchemaFieldName
)
