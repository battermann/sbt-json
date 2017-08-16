package j2cgen.models

import j2cgen.models.Schema.{SchemaFieldName, SchemaObjectName}

case class SchemaNameGenerator(
  genClassName: String => SchemaObjectName,
  genFieldName: String => SchemaFieldName
)
