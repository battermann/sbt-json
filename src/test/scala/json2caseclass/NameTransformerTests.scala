package json2caseclass

import org.scalatest._

class NameTransformerTests extends FlatSpec with Matchers {

  "Name with invalid chars" should "match pattern" in {
    val name = "rfdfm453)§$"
    implementation.NameTransformer.containsInvalidChars(name) shouldBe true
  }
}

