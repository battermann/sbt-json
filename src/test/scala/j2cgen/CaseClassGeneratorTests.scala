package j2cgen

import j2cgen.CaseClassGenerator.generate
import j2cgen.CaseClassToStringInterpreter._
import j2cgen.SchemaExtractorOptions._
import j2cgen.models.CaseClass.{ClassFieldName, ClassName}
import j2cgen.models.json.ToJsonString
import j2cgen.models.{ArrayEmpty, JsonContainsNoObjects}
import org.scalatest._
import play.api.libs.json.{Json, OFormat}

class CaseClassGeneratorTests extends FlatSpec with Matchers {

  "Generated case class source from json string derived from 'case class R00t(hello: String)'" should "equal 'case class R00t(hello: String) ...' with play json formats" in {
    object autoImport {

      case class R00t(hello: String)

      implicit val fooFormat: OFormat[autoImport.R00t] = Json.format[R00t]
    }

    import autoImport._

    val foo = R00t("world")
    val jsonString = Json.toJson(foo)

    val caseClassSource = generate(interpreter = plainCaseClasses.withPlayJsonFormats)(jsonString.toString.toJsonString, "R00t".toRootTypeName)

    val expected =
      """case class R00t(
        |  hello: String
        |)
        |
        |object R00t {
        |  import play.api.libs.json.Json
        |
        |  implicit val formatR00t = Json.format[R00t]
        |}
        |""".stripMargin

    caseClassSource shouldEqual Right(expected)
  }

  "Generated source from json of empty object" should "contain empty root class" in {
    val jsonString = "{}"

    val resultOrError = generate()(jsonString.toJsonString, "R00t".toRootTypeName)

    val expected ="case class R00t(\n)\n"

    resultOrError shouldEqual Right(expected)
  }

  "Generated source with optional option" should "contain correct optional fields" in {
    val jsonString =
      """
        |{
        |  "foo": { "x": 1 },
        |  "value1": false,
        |  "value2": "hello"
        |}
      """.stripMargin

    val resultOrError = generate()(jsonString.toJsonString, "R00t".toRootTypeName, Seq(("Foo", "x"), ("R00t", "foo"), ("R00t", "value2")).asInstanceOf[Seq[(ClassName, ClassFieldName)]])

    val expected =
      """case class R00t(
        |  foo: Option[Foo],
        |  value1: Boolean,
        |  value2: Option[String]
        |)
        |
        |case class Foo(
        |  x: Option[Double]
        |)
        |""".stripMargin

    resultOrError shouldEqual Right(expected)
  }

  "Generated source from json with array of objects" should "contain root class that represents the type of the array" in {
    val jsonString =
      """
        |[
        |  { "x": 1 },
        |  { "x": 2 }
        |]
      """.stripMargin

    val resultOrError = generate()(jsonString.toJsonString, "R00t".toRootTypeName)

    val expected =
      """case class R00t(
        |  x: Double
        |)
        |""".stripMargin

    resultOrError shouldEqual Right(expected)
  }

  "If IgnoreEmptyArray and raw json contains an empty array the result" should "not contain that array" in {
    val jsonString =
      """
        |{
        |  "foo": 1,
        |  "bar": [],
        |  "qwertz": [
        |    1,
        |    2,
        |    3
        |  ],
        |  "lists": [
        |    [ 1, 2, 3],
        |    [ 4 ],
        |    [],
        |    [ 5, 6 ]
        |  ]
        |}
        |""".stripMargin

    val caseClassSource = generate(include = exceptEmptyArrays(includeAll))(jsonString.toJsonString, "R00t".toRootTypeName)

    val expected =
      """case class R00t(
        |  foo: Double,
        |  qwertz: Seq[Double],
        |  lists: Seq[Seq[Double]]
        |)
        |""".stripMargin

    caseClassSource shouldEqual Right(expected)
  }

  "If IgnoreNullValues and raw json contains a null value the result" should "not contain that value" in {
    val jsonString =
      """
        |{
        |  "foo": 1,
        |  "bar": null,
        |  "list": [ "hello", null, "world" ]
        |}
        |""".stripMargin

    val caseClassSource = generate(include = exceptNullValues(includeAll))(jsonString.toJsonString, "R00t".toRootTypeName)

    val expected =
      """case class R00t(
        |  foo: Double,
        |  list: Seq[String]
        |)
        |""".stripMargin

    caseClassSource shouldEqual Right(expected)
  }

  "If there are objects with exactly the same structure the result" should "contain only the first object and all other references should be replaced" in {
    val jsonString =
      """
        |{
        |  "appointment": { "location": { "x": 1, "y": 2 }, "time": 43534243534 },
        |  "game_start": { "location": { "x": 1, "y": 2 }, "time": 43534243534 },
        |  "location": { "x": 1, "y": 2 },
        |  "other_location": { "x": 5, "y": 6 }
        |}
        |""".stripMargin

    val caseClassSource = generate()(jsonString.toJsonString, "R00t".toRootTypeName)

    val expected =
      """case class R00t(
        |  appointment: Appointment,
        |  game_start: Appointment,
        |  location: Location,
        |  other_location: Location
        |)
        |
        |case class Appointment(
        |  location: Location,
        |  time: Double
        |)
        |
        |case class Location(
        |  x: Double,
        |  y: Double
        |)
        |""".stripMargin

    caseClassSource shouldEqual Right(expected)
  }

  "Generated case class of json with nested array" should "contain a nested sequence" in {
    val jsonString =
      """
        |{
        |  "foo": [ [1,2], [3,4] ],
        |  "bar": "baz"
        |}
        |""".stripMargin

    val caseClassSource = generate(include = exceptEmptyArrays(includeAll))(jsonString.toJsonString, "R00t".toRootTypeName)

    val expected =
      """case class R00t(
        |  foo: Seq[Seq[Double]],
        |  bar: String
        |)
        |""".stripMargin

    caseClassSource shouldEqual Right(expected)
  }

  "Generated case classes of json with array containing different objects" should "contain a class with optional fields" in {
    val jsonString =
      """
        |{
        |  "objects": [
        |    { "field1": "foo", "field5": 2 },
        |    { "field1": "bar", "field5": 3 },
        |    { "field2": 42, "field3": false,"field5": 4 },
        |    { "field3": true, "field4": [ 1, 2, 3], "field5": 5 }
        |   ]
        |}
        |""".stripMargin

    val caseClassSource = generate()(jsonString.toJsonString, "R00t".toRootTypeName)

    val expected =
      """case class R00t(
        |  objects: Seq[Objects]
        |)
        |
        |case class Objects(
        |  field5: Double,
        |  field1: Option[String],
        |  field2: Option[Double],
        |  field3: Option[Boolean],
        |  field4: Option[Seq[Double]]
        |)
        |""".stripMargin

    caseClassSource shouldEqual Right(expected)
  }

  "Generated source from json with array of numbers" should "fail with JsonContainsNoObjects" in {
    val jsonString = "[ 1, 2, 3 ]"

    val resultOrError = generate()(jsonString.toJsonString, "R00t".toRootTypeName)

    val expected = Left(JsonContainsNoObjects)

    resultOrError shouldEqual expected
  }

  "Generated source from json with empty array root" should "fail with ArrayEmpty" in {
    val jsonString = "[]"

    val resultOrError = generate()(jsonString.toJsonString, "R00t".toRootTypeName)

    val expected = Left(ArrayEmpty("R00t"))

    resultOrError shouldEqual expected
  }

  "Generated names that equals a scala reserved word" should "have suffix or back-ticks" in {
    val jsonString = """{ "type": { "else": 1 } }"""

    val resultOrError = generate()(jsonString.toJsonString, "R00t".toRootTypeName)

    val expected = Right(
      """case class R00t(
        |  `type`: TypeModel
        |)
        |
        |case class TypeModel(
        |  `else`: Double
        |)
        |""".stripMargin)

    resultOrError shouldEqual expected
  }

  "Generated source from json of object with two values of different types" should "contain three unique class classes" in {
    val jsonString =
      """
        |{
        |  "value1": {
        |    "bar": 42
        |  },
        |  "value2": {
        |    "bar": "Hello!"
        |  }
        |}
      """.stripMargin

    val resultOrError = generate()(jsonString.toJsonString, "R00t".toRootTypeName)

    val expected =
      """case class R00t(
        |  value1: Value1,
        |  value2: Value2
        |)
        |
        |case class Value1(
        |  bar: Double
        |)
        |
        |case class Value2(
        |  bar: String
        |)
        |""".stripMargin

    resultOrError shouldEqual Right(expected)
  }

  "Generated source from json of object type with two values of object with field of same type with same name" should "contain three unique class classes" in {
    val jsonString =
      """
        |{
        |  "value1": {
        |    "bar": { "value": 42 }
        |  },
        |  "value2": {
        |    "bar": { "value": 1337 }
        |  }
        |}
      """.stripMargin

    val resultOrError = generate()(jsonString.toJsonString, "R00t".toRootTypeName)

    val expected =
      """case class R00t(
        |  value1: Value1,
        |  value2: Value1
        |)
        |
        |case class Value1(
        |  bar: Bar
        |)
        |
        |case class Bar(
        |  value: Double
        |)
        |""".stripMargin

    resultOrError shouldEqual Right(expected)
  }

  "Generated source from json of object type with two values of object with field with different name and same type" should "contain four unique class classes" in {
    val jsonString =
      """
        |{
        |  "value1": {
        |    "bar1": { "value": 42 }
        |  },
        |  "value2": {
        |    "bar2": { "value": 1337 }
        |  }
        |}
      """.stripMargin

    val resultOrError = generate()(jsonString.toJsonString, "R00t".toRootTypeName)

    val expected =
      """case class R00t(
        |  value1: Value1,
        |  value2: Value2
        |)
        |
        |case class Value1(
        |  bar1: Bar1
        |)
        |
        |case class Bar1(
        |  value: Double
        |)
        |
        |case class Value2(
        |  bar2: Bar1
        |)
        |""".stripMargin

    resultOrError shouldEqual Right(expected)
  }

  "Generated source from json of object type with two values of object with field with same name and different type" should "contain four unique class classes" in {
    val jsonString =
      """
        |{
        |  "value1": {
        |    "bar": { "value": 42 }
        |  },
        |  "value2": {
        |    "bar": { "value": "some string" }
        |  }
        |}
      """.stripMargin

    val resultOrError = generate()(jsonString.toJsonString, "R00t".toRootTypeName)

    val expected =
      """case class R00t(
        |  value1: Value1,
        |  value2: Value2
        |)
        |
        |case class Value1(
        |  bar: Bar
        |)
        |
        |case class Bar(
        |  value: Double
        |)
        |
        |case class Value2(
        |  bar: Bar1
        |)
        |
        |case class Bar1(
        |  value: String
        |)
        |""".stripMargin

    resultOrError shouldEqual Right(expected)
  }
}
