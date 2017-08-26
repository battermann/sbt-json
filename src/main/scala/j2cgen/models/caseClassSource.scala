package j2cgen.models

import shapeless.tag.@@

object caseClassSource {
  trait CaseClassSourceTag
  type CaseClassSource = String @@ CaseClassSourceTag

  implicit class ToCaseClassSource(caseClassSource: String) {
    def toCaseClassSource: CaseClassSource = caseClassSource.asInstanceOf[CaseClassSource]
  }
}
