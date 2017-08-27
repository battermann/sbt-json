package j2cgen

import j2cgen.models.CaseClass._

object CaseClassNameGenerator {
  def makeUnique(reservedClassNames: Set[String], name: ClassName): Option[ClassName] = {
    val suffixes = "" #:: Stream.from(1).map(_.toString)
    val namesWithSuffix = suffixes.map(suffix => s"$name$suffix")
    namesWithSuffix.find(nameWithSuffix => !reservedClassNames.contains(nameWithSuffix))
      .map(_.toClassName)
  }
}
