package json2caseclass.implementation

import cats.data.ReaderWriterStateT._
import cats.implicits._
import json2caseclass.model.Schema._
import json2caseclass.model.Types.{ErrorOr, ErrorRWSOr, JsValueFilter, RootTypeName}
import json2caseclass.model._
import play.api.libs.json._

object SchemaExtractor {

  def extractSchemaFromJsonRoot(rootTypeName: RootTypeName, value: JsValue): ErrorRWSOr[Schema] = {
    value match {
      case JsObject(fields) =>
        extractSchemaFromJsObjectFields(rootTypeName, fields.toList)
      case JsArray(values) =>
        extractSchemaFromArray(rootTypeName, values.toList)
      case _ =>
        ? <~ Left(InvalidRoot)
    }
  }

  private def extractSchemaFromJsObjectFields(name: String, fields: List[(String, JsValue)]): ErrorRWSOr[Schema] = {
    for {
      env <- ask[ErrorOr, Environment, Unit, Int]
      fieldSchemas <- fields
        .filter { case (_, value) => env.jsValueFilter(value) }
        .map { case (fieldName, value) => extractSchemaFromJsValue(fieldName, value) }.sequence
      schema <- mkSchemaObject(
        env.nameTransformer.makeSafeCamelCaseClassName(name),
        fieldSchemas.map { case (n, v) => (env.nameTransformer.makeSafeFieldName(n), v) }
      )
    } yield schema
  }

  private def extractSchemaFromJsValue(name: String, value: JsValue): ErrorRWSOr[(String, Schema)] = {
    val schemaOrError = value match {
      case JsNull =>
        ? <~ Left(ValueIsNull(name))
      case JsString(_) =>
        ? <~ Right(SchemaString)
      case JsNumber(_) =>
        ? <~ Right(SchemaDouble)
      case JsBoolean(_) =>
        ? <~ Right(SchemaBoolean)
      case JsObject(fields) =>
        extractSchemaFromJsObjectFields(name, fields.toList)
      case JsArray(values) =>
        extractSchemaFromArray(name, values.toList)
    }
    schemaOrError.map((name, _))
  }

  private def isObject(schema: Schema) = {
    schema match {
      case SchemaObject(_, _, _) => true
      case _ => false
    }
  }

  private def extractSchemaFromArray(name: String, values: List[JsValue]): ErrorRWSOr[Schema] = {
    for {
      env <- ask[ErrorOr, Environment, Unit, Int]
      schemas <- values
        .filter(value => env.jsValueFilter(value))
        .map(value => extractSchemaFromJsValue(name, value).map(_._2))
        .sequence
      first <- ? <~ schemas.headOption.toRight(ArrayEmpty(name))
      schemaErrorRWSOr = if (schemas forall (_ == first)) {
        ? <~ Right(SchemaArray(first))
      } else if (schemas forall isObject) {
        mkSchemaObject(
          env.nameTransformer.makeSafeCamelCaseClassName(name),
          unify(schemas))
          .map(SchemaArray)
      } else {
        ? <~ Left(ArrayTypeNotConsistent(name))
      }
      schema <- schemaErrorRWSOr
    } yield schema
  }

  private def mkSchemaObject(name: SchemaObjectName, fields: Seq[(SchemaFieldName, Schema)]) = {
    for {
      id <- get[ErrorOr, Environment, Unit, Int]
      _ <- modify[ErrorOr, Environment, Unit, Int]((_: Int) + 1)
    } yield SchemaObject(
      id,
      name,
      fields
    )
  }

  private def unify(schemas: List[Schema]): List[(SchemaFieldName, Schema)] = {
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
    combinedFields.asInstanceOf[List[(SchemaFieldName, Schema)]]
  }

  val allJsValues: JsValueFilter = _ => true

  val exceptEmptyArrays: JsValueFilter => JsValueFilter = {
    include => {
      case JsArray(values) if values.isEmpty => false
      case jsValue => include(jsValue)
    }
  }

  val exceptNullValues: JsValueFilter => JsValueFilter = {
    include => {
      case JsNull => false
      case jsValue => include(jsValue)
    }
  }

  implicit class IncludeOptions(include: JsValueFilter) {
    def exceptEmptyArrays = SchemaExtractor.exceptEmptyArrays(include)

    def exceptNullValues = SchemaExtractor.exceptNullValues(include)
  }

}
