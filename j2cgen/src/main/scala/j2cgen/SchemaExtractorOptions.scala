package j2cgen

import play.api.libs.json.{JsArray, JsNull, JsValue}
import shapeless.tag.@@

object SchemaExtractorOptions {

  type Include = JsValue => Boolean

  trait RootTypeNameTag
  type RootTypeName = String @@ RootTypeNameTag

  implicit class ToRootTypeName(name: String) {
    def toRootTypeName: RootTypeName = name.asInstanceOf[RootTypeName]
  }

  val includeAll: Include = _ => true

  val exceptEmptyArrays: Include => Include = { include =>
    jsValue => include(jsValue) && (jsValue match {
      case JsArray(values) if values.isEmpty => false
      case _ => true
    })
  }

  val exceptNullValues: Include => Include = { include =>
    jsValue => include(jsValue) && (jsValue match {
      case JsNull => false
      case _ => true
    })
  }
}
