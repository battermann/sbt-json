package json2caseclass.model

import json2caseclass.model.ScalaType.ScalaObjectName
import shapeless.tag
import shapeless.tag.@@

sealed trait ScalaType
case class ScalaOption(t: ScalaType) extends ScalaType
case object ScalaString extends ScalaType
case object ScalaBoolean extends ScalaType
case object ScalaDouble extends ScalaType
case class ScalaSeq(t: ScalaType) extends ScalaType
case class ScalaObject(id: Int, name: ScalaObjectName) extends ScalaType

object ScalaType {
  trait ScalaObjectNameTag
  type ScalaObjectName = String @@ ScalaObjectNameTag

  implicit class ToScalaObjectName(name: String) {
    def toScalaObjectName: ScalaObjectName = tag[ScalaObjectNameTag][String](name)
  }
}