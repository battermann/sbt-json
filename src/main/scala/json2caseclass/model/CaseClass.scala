package json2caseclass.model

import java.util.UUID

import json2caseclass.model.CaseClass.{ClassFieldName, ClassId, ClassName}
import shapeless.tag
import shapeless.tag.@@

case class CaseClass(id: ClassId, name: ClassName, fields: Seq[(ClassFieldName, ScalaType)])

object CaseClass {

  trait ClassIdTag
  type ClassId = UUID @@ ClassIdTag

  trait ClassNameTag
  type ClassName = String @@ ClassNameTag

  trait ClassFieldNameTag
  type ClassFieldName = String @@ ClassFieldNameTag

  implicit class ToClassId(uuid: UUID) {
    def toClassId: ClassId = tag[ClassIdTag][UUID](uuid)
  }

  implicit class ToClassName(name: String) {
    def toClassName: ClassName = tag[ClassNameTag][String](name)
  }

  implicit class ToClassFieldName(name: String) {
    def toClassFieldName: ClassFieldName = tag[ClassFieldNameTag][String](name)
  }
}
