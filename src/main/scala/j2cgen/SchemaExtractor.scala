package j2cgen

import java.util.UUID

import cats.implicits._
import j2cgen.SchemaExtractorOptions.{JsValueFilter, RootTypeName}
import j2cgen.models.Schema._
import j2cgen.models._
import play.api.libs.json._


object SchemaExtractor {

  def extractSchemaFromJsonRoot(
    jsValueFilter: JsValueFilter,
    nameGenerator: SchemaNameGenerator,
    value: JsValue,
    root: RootTypeName): Either[CaseClassGenerationFailure, Schema] = {
    value match {
      case JsObject(fields) =>
        extractSchemaFromJsObjectFields(jsValueFilter, nameGenerator, root, fields.toList)
      case JsArray(values) =>
        extractSchemaFromArray(jsValueFilter, nameGenerator, root, values)
      case _ =>
        Left(InvalidRoot)
    }
  }

  private def extractSchemaFromJsObjectFields(
    jsValueFilter: JsValueFilter,
    nameGenerator: SchemaNameGenerator,
    name: String,
    fields: List[(String, JsValue)]): Either[CaseClassGenerationFailure, Schema] = {
    fields
      .filter { case (_, value) => jsValueFilter(value) }
      .map { case (fieldName, value) =>
        extractSchemaFromJsValue(jsValueFilter, nameGenerator, fieldName, value).leftMap {
          case ValueIsNull(_) => ValueIsNull(s"$name.$fieldName")
          case other => other
        }
      }
      .sequenceU
      .map(schemaFields => SchemaObject(UUID.randomUUID().toSchemaObjectId, nameGenerator.genClassName(name),
        schemaFields.map { case (n, v) => (nameGenerator.genFieldName(n), v) }))
  }

  private def extractSchemaFromJsValue(
    jsValueFilter: JsValueFilter,
    nameGenerator: SchemaNameGenerator,
    name: String,
    value: JsValue): Either[CaseClassGenerationFailure, (String, Schema)] = {
    val schemaOrError = value match {
      case JsNull =>
        Left(ValueIsNull(name))
      case JsString(_) =>
        Right(SchemaString)
      case JsNumber(_) =>
        Right(SchemaDouble)
      case JsBoolean(_) =>
        Right(SchemaBoolean)
      case JsObject(fields) =>
        extractSchemaFromJsObjectFields(jsValueFilter, nameGenerator, name, fields.toList)
      case JsArray(values) =>
        extractSchemaFromArray(jsValueFilter, nameGenerator, name, values)
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
    jsValueFilter: JsValueFilter,
    nameGenerator: SchemaNameGenerator,
    name: String,
    values: Seq[JsValue]): Either[CaseClassGenerationFailure, Schema] = {
    val schemasOrError =
      values
        .filter(value => jsValueFilter(value))
        .map(value => extractSchemaFromJsValue(jsValueFilter, nameGenerator, name, value).map(_._2))
        .toList
        .sequenceU

    for {
      schemas <- schemasOrError
      first <- schemas.headOption.toRight(ArrayEmpty(name))
      schema <- if (schemas forall (_ == first)) {
        Right(SchemaArray(first))
      } else if (schemas forall isObject) {
        val schema = SchemaObject(UUID.randomUUID().toSchemaObjectId, nameGenerator.genClassName(name),
          unify(schemas))
        Right(SchemaArray(schema))
      } else {
        Left(ArrayTypeNotConsistent(name))
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
