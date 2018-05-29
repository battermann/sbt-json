package json2caseclass.implementation

import cats.implicits._
import json2caseclass.model.CaseClass._
import json2caseclass.model.ScalaType._
import json2caseclass.model._
import util.CollectionUtils._

object SchemaToCaseClassConverter {

  def convert(schema: Schema): Either[CaseClassGenerationFailure, Seq[CaseClass]] = {
    Right[CaseClassGenerationFailure, Seq[SchemaObject]](findAllSObjects(schema))
      .ensure(JsonContainsNoObjects)(_.nonEmpty)
      .map(removeDuplicates)
      .map(_.map(schemaObjectToCaseClass))
  }

  private def findAllSObjects(schema: Schema): Seq[SchemaObject] = {
    def findRecursively(schema: Schema, acc: Seq[SchemaObject]): Seq[SchemaObject] = {
      schema match {
        case o @ SchemaObject(_, _, fields) =>
          val nestedObjects =
            fields
              .flatMap {
                case (_, fieldType) => findRecursively(fieldType, Seq())
              }
          o +: (acc ++ nestedObjects)
        case SchemaArray(arrayType) =>
          findRecursively(arrayType, acc)
        case SchemaOption(optionType) =>
          findRecursively(optionType, acc)
        case _ => acc
      }
    }

    findRecursively(schema, Seq())
  }

  private def removeDuplicates(schemaObjects: Seq[SchemaObject]): Seq[SchemaObject] = {
    val duplicates =
      findDuplicates(schemaObjects)(o => o.id, Schema.haveSameStructure)

    schemaObjects
      .filter(o => !duplicates.contains(o.id))
      .map {
        case o @ SchemaObject(_, _, fields) =>
          val xs = fields.map {
            case (fieldName, fieldSchema) =>
              fieldSchema match {
                case SchemaObject(otherId, _, _) =>
                  (fieldName, duplicates.getOrElse(otherId, fieldSchema))
                case _ =>
                  (fieldName, fieldSchema)
              }
          }
          o.copy(fields = xs)
      }
  }

  private def schemaObjectToCaseClass(schemaObject: SchemaObject): CaseClass = {
    schemaObject match {
      case SchemaObject(id, name, fields) =>
        val caseClassFields = fields.map {
          case (fieldName, schema) =>
            (fieldName.toClassFieldName, schemaToScalaType(schema))
        }
        CaseClass(id, name.toClassName, caseClassFields)
    }
  }

  private def schemaToScalaType(schema: Schema): ScalaType = {
    schema match {
      case SchemaOption(s)           => ScalaOption(schemaToScalaType(s))
      case SchemaString              => ScalaString
      case SchemaBoolean             => ScalaBoolean
      case SchemaDouble              => ScalaDouble
      case SchemaArray(s)            => ScalaSeq(schemaToScalaType(s))
      case SchemaObject(id, name, _) => ScalaObject(id, name.toScalaObjectName)
    }
  }
}
