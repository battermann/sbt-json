package j2cgen.models

import java.util.UUID

import j2cgen.models.ScalaType.{ScalaObjectId, ScalaObjectName}
import shapeless.tag.@@

sealed trait ScalaType
case class ScalaOption(t: ScalaType) extends ScalaType
case object ScalaString extends ScalaType
case object ScalaBoolean extends ScalaType
case object ScalaDouble extends ScalaType
case class ScalaSeq(t: ScalaType) extends ScalaType
case class ScalaObject(id: ScalaObjectId, name: ScalaObjectName) extends ScalaType

object ScalaType {
  trait ScalaObjectIdTag
  type ScalaObjectId = UUID @@ ScalaObjectIdTag

  trait ScalaObjectNameTag
  type ScalaObjectName = String @@ ScalaObjectNameTag

  implicit class ToScalaObjectId(uuid: UUID) {
    def toScalaObjectId: ScalaObjectId = uuid.asInstanceOf[ScalaObjectId]
  }

  implicit class ToScalaObjectName(name: String) {
    def toScalaObjectName: ScalaObjectName = name.asInstanceOf[ScalaObjectName]
  }
}