package json2caseclass

import org.scalatest._
import play.api.libs.json.Json

class GeneralTests extends FlatSpec with Matchers {

  "Case class instance that is converted to json string and back to instance using play json formats" should "equal original instance" in {

    object autoImport {

      case class R00t(hello: String)

      implicit val fooFormat = Json.format[R00t]
    }

    import autoImport._

    val foo = R00t("world")
    val jsonString = Json.toJson(foo).toString
    val convertedBack = Json.parse(jsonString).as[R00t]
    convertedBack shouldEqual foo
  }

  "Json that represents a sequence of numbers" should "be castable to a Seq[Double]" in {

    object NumberList {
      type NumberList = Seq[Double]
    }

    import NumberList._

    val jsonString = """[ 1, 2, 3 ]"""

    val jsonValue = Json.parse(jsonString)

    val data = jsonValue.as[NumberList]

    data.head shouldEqual 1
  }
}

