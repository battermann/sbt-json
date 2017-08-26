package j2cgen.models

import shapeless.tag.@@

object json {
  trait JsonStringTag
  type JsonString = String @@ JsonStringTag

  implicit class ToJsonString(json: String) {
    def toJsonString: JsonString = json.asInstanceOf[JsonString]
  }
}
