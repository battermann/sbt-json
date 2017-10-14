package json2caseclass.model

import json2caseclass.model.Schema.{SchemaFieldName, SchemaObjectName}

case class SchemaNameGenerator(
  genClassName: String => SchemaObjectName,
  genFieldName: String => SchemaFieldName
)
