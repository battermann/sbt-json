package j2cgen.models

import java.util.UUID

import j2cgen.models.CaseClass.{ClassFieldName, ClassId, ClassName}
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
    def toClassId: ClassId = uuid.asInstanceOf[ClassId]
  }

  implicit class ToClassName(name: String) {
    def toClassName: ClassName = name.asInstanceOf[ClassName]
  }

  implicit class ToClassFieldName(name: String) {
    def toClassFieldName: ClassFieldName = name.asInstanceOf[ClassFieldName]
  }
}
