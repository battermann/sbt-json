package json2caseclass.model

import json2caseclass.model.Schema.{SchemaFieldName, SchemaObjectName}
import shapeless.tag
import shapeless.tag.@@

sealed trait Schema
case class SchemaOption(schema: Schema) extends Schema
case object SchemaString extends Schema
case object SchemaBoolean extends Schema
case object SchemaDouble extends Schema
case class SchemaArray(schema: Schema) extends Schema
case class SchemaObject(id: Int, name: SchemaObjectName, fields: Seq[(SchemaFieldName, Schema)]) extends Schema

object Schema {

  trait SchemaObjectNameTag
  type SchemaObjectName = String @@ SchemaObjectNameTag

  trait SchemaFieldNameTag
  type SchemaFieldName = String @@ SchemaFieldNameTag

  implicit class ToSchemaFieldName(name: String) {
    def toSchemaFieldName: SchemaFieldName = tag[SchemaFieldNameTag][String](name)
  }

  implicit class ToSchemaObjectName(name: String) {
    def toSchemaObjectName: SchemaObjectName = tag[SchemaObjectNameTag][String](name)
  }

  def haveSameStructure(a: Schema, b: Schema): Boolean = {
    (a, b) match {
      case (SchemaObject(_, _, fields1), SchemaObject(_, _, fields2)) =>
        fields1.size == fields2.size && fields1.forall{ case (n1, e1) => fields2.exists { case (n2, e2) => n1 == n2 && haveSameStructure(e1, e2) }}
      case (SchemaArray(schema1), SchemaArray(schema2)) =>
        haveSameStructure(schema1, schema2)
      case (SchemaOption(schema1), SchemaOption(schema2)) =>
        haveSameStructure(schema1, schema2)
      case _ => a == b
    }
  }
}
