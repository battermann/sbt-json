package json2caseclass

import play.api.libs.json.{JsArray, JsNull, JsValue}
import shapeless.tag
import shapeless.tag.@@

object SchemaExtractorOptions {

  type JsValueFilter = JsValue => Boolean

  trait RootTypeNameTag

  type RootTypeName = String @@ RootTypeNameTag

  implicit class ToRootTypeName(name: String) {
    def toRootTypeName: RootTypeName = tag[RootTypeNameTag][String](name)
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
    def exceptEmptyArrays = SchemaExtractorOptions.exceptEmptyArrays(include)
    def exceptNullValues = SchemaExtractorOptions.exceptNullValues(include)
  }
}
