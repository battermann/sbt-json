package json2caseclass.model

import shapeless.tag
import shapeless.tag.@@

object caseClassSource {
  trait CaseClassSourceTag
  type CaseClassSource = String @@ CaseClassSourceTag

  implicit class ToCaseClassSource(caseClassSource: String) {
    def toCaseClassSource: CaseClassSource = tag[CaseClassSourceTag][String](caseClassSource)
  }
}
