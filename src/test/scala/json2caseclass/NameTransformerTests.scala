package json2caseclass

import org.scalatest._
import json2caseclass.model.Schema._
import json2caseclass.model.Types._

class NameTransformerTests extends FlatSpec with Matchers {

  "Name with invalid chars" should "match pattern" in {
    val name = "rfdfm453)§$"
    implementation.NameTransformer.containsInvalidChars(name) shouldBe true
  }

  "Invalid object names" should "be transformed correctly" in {
    val objectNames = List(
      "foo" -> "Foo".toSchemaObjectName,
      "match" -> "MatchModel".toSchemaObjectName,
      "foo-bar" -> "FooBar".toSchemaObjectName,
      "3foo" -> "Foo".toSchemaObjectName,
      "_foobar" -> "Foobar".toSchemaObjectName
    )

    objectNames foreach { case(in, out) =>
      implementation.NameTransformer.makeSafeCamelCaseCaseClassName("Model".toSuffix)(in) shouldEqual out
    }
  }

  "Invalid field names" should "be transformed correctly" in {
    val invalidFieldNames = List(
      "foo-bar",
      "3foo",
      "foo%bar",
      "case",
      "match"
    )

    invalidFieldNames foreach { n =>
      implementation.NameTransformer.makeSafeFieldName(n) shouldEqual s"`$n`"
    }
  }

  "Valid field names" should "not be changed" in {
    val validFieldNames = List(
      "foo",
      "foo3",
      "_foo3",
      "foo_bar"
    )

    validFieldNames foreach { n =>
      implementation.NameTransformer.makeSafeFieldName(n) shouldEqual n
    }
  }
}

