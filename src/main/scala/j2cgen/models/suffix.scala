package j2cgen.models

import shapeless.tag.@@

object suffix {
  trait SuffixTag
  type Suffix = String @@ SuffixTag

  implicit class ToSuffix(name: String) {
    def toSuffix: Suffix = name.asInstanceOf[Suffix]
  }
}
