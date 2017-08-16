package util

import org.scalatest.{FlatSpec, Matchers}

class CollectionUtilTests extends FlatSpec with Matchers {
  "A list which contains 3 duplicates" should "yield a map of the duplicate's IDs with the 2 unique original objects" in {

    case class Data(id: Int, value: String)

    val xs = Seq(
      Data(1, "a"),
      Data(2, "b"),
      Data(3, "b"),
      Data(4, "c"),
      Data(5, "b"),
      Data(6, "c"),
      Data(7, "d")
    )

    val expected = Map(
      3 -> Data(2, "b"),
      5 -> Data(2, "b"),
      6 -> Data(4, "c")
    )

    CollectionUtils.findDuplicates(xs)(x => x.id, (a, b) => a.value == b.value) shouldEqual expected
  }
}
