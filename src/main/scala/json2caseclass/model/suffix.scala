package json2caseclass.model

import shapeless.tag
import shapeless.tag.@@

object suffix {
  trait SuffixTag
  type Suffix = String @@ SuffixTag

  implicit class ToSuffix(name: String) {
    def toSuffix: Suffix = tag[SuffixTag][String](name)
  }
}
