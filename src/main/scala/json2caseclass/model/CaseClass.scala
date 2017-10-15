package json2caseclass.model

import json2caseclass.model.CaseClass.{ClassFieldName, ClassName}
import shapeless.tag
import shapeless.tag.@@

case class CaseClass(id: Int, name: ClassName, fields: Seq[(ClassFieldName, ScalaType)])

object CaseClass {
  trait ClassNameTag
  type ClassName = String @@ ClassNameTag

  trait ClassFieldNameTag
  type ClassFieldName = String @@ ClassFieldNameTag

  implicit class ToClassName(name: String) {
    def toClassName: ClassName = tag[ClassNameTag][String](name)
  }

  implicit class ToClassFieldName(name: String) {
    def toClassFieldName: ClassFieldName = tag[ClassFieldNameTag][String](name)
  }
}
