package j2cgen

import java.util.UUID

import cats.implicits._
import j2cgen.SchemaExtractorOptions.{Include, RootTypeName}
import j2cgen.models.Schema._
import j2cgen.models._
import play.api.libs.json._


object SchemaExtractor {

  def extractSchemaFromJsonRoot(
    include: Include,
    nameGenerator: SchemaNameGenerator,
    value: JsValue,
    root: RootTypeName): Either[CaseClassGenerationFailure, Schema] = {
    value match {
      case JsObject(fields) =>
        extractSchemaFromJsObjectFields(include, nameGenerator, root, fields.toList)
      case JsArray(values) =>
        extractSchemaFromArray(include, nameGenerator, root, values)
      case _ =>
        Left(InvalidRoot)
    }
  }

  private def extractSchemaFromJsObjectFields(
    include: Include,
    nameGenerator: SchemaNameGenerator,
    name: String,
    fields: List[(String, JsValue)]): Either[CaseClassGenerationFailure, Schema] = {
    fields
      .filter { case (_, value) => include(value) }
      .map { case (fieldName, value) =>
        extractSchemaFromJsValue(include, nameGenerator, fieldName, value)
      }
      .sequenceU
      .map(schemaFields => SchemaObject(UUID.randomUUID().toSchemaObjectId, nameGenerator.genClassName(name),
        schemaFields.map { case (n, v) => (nameGenerator.genFieldName(n), v) }))
  }

  private def extractSchemaFromJsValue(
    include: Include,
    nameGenerator: SchemaNameGenerator,
    name: String,
    value: JsValue): Either[CaseClassGenerationFailure, (String, Schema)] = {
    val schemaOrError = value match {
      case JsNull =>
        Left(ValueIsNull)
      case JsString(_) =>
        Right(SchemaString)
      case JsNumber(_) =>
        Right(SchemaDouble)
      case JsBoolean(_) =>
        Right(SchemaBoolean)
      case JsObject(fields) =>
        extractSchemaFromJsObjectFields(include, nameGenerator, name, fields.toList)
      case JsArray(values) =>
        extractSchemaFromArray(include, nameGenerator, name, values)
    }
    schemaOrError.map((name, _))
  }

  private def isObject(schema: Schema) = {
    schema match {
      case SchemaObject(_, _, _) => true
      case _ => false
    }
  }

  private def extractSchemaFromArray(
    include: Include,
    nameGenerator: SchemaNameGenerator,
    name: String,
    values: Seq[JsValue]): Either[CaseClassGenerationFailure, Schema] = {
    val schemasOrError =
      values
        .filter(value => include(value))
        .map(value => extractSchemaFromJsValue(include, nameGenerator, name, value).map(_._2))
        .toList
        .sequenceU

    for {
      schemas <- schemasOrError
      first <- schemas.headOption.toRight(ArrayEmpty)
      schema <- if (schemas forall (_ == first)) {
        Right(SchemaArray(first))
      } else if (schemas forall isObject) {
        val schema = SchemaObject(UUID.randomUUID().toSchemaObjectId, nameGenerator.genClassName(name),
          unify(schemas))
        Right(SchemaArray(schema))
      } else {
        Left(ArrayTypeNotConsistent)
      }
    } yield schema
  }

  private def unify(schemas: Seq[Schema]): Seq[(SchemaFieldName, Schema)] = {
    def containsField(t: Schema, field: (String, Schema)) = {
      t match {
        case SchemaObject(_, _, xs) =>
          xs.contains(field)
        case _ => false
      }
    }

    val allFields = schemas.flatMap {
      case SchemaObject(_, _, fields) => fields
      case _ => Nil
    }

    val (required, optionals) =
      allFields
        .distinct
        .partition(field => schemas.forall(schema => containsField(schema, field)))

    val combinedFields = required ++ optionals.map { case (n, s) => (n, SchemaOption(s)) }
    combinedFields.asInstanceOf[Seq[(SchemaFieldName, Schema)]]
  }
}
