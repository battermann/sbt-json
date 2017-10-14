package json2caseclass

import json2caseclass.model.CaseClass._

object CaseClassNameGenerator {
  def makeUnique(takenClassNames: Set[String], name: ClassName): Option[ClassName] = {
    val suffixes = "" #:: Stream.from(1).map(_.toString)
    val namesWithSuffix = suffixes.map(suffix => s"$name$suffix")
    namesWithSuffix.find(nameWithSuffix => !takenClassNames.contains(nameWithSuffix))
      .map(_.toClassName)
  }
}
