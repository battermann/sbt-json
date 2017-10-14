package json2caseclass.model

import shapeless.tag
import shapeless.tag.@@

object json {
  trait JsonStringTag
  type JsonString = String @@ JsonStringTag

  implicit class ToJsonString(json: String) {
    def toJsonString: JsonString = tag[JsonStringTag][String](json)
  }
}
