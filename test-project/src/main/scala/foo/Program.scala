package foo

import models.json.foo._

import play.api.libs.json._

object Program extends App {
  val jsonString = """{ "foo": 42 }"""

  val foo = Json.parse(jsonString).as[Foo]

  println(foo.foo)
}