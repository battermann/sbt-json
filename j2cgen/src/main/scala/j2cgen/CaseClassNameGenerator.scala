package j2cgen

import j2cgen.models.CaseClass.{ClassName, ToClassName}

object CaseClassNameGenerator {
  def makeUnique(reservedClassNames: Set[String], name: ClassName): Option[ClassName] = {
    val suffixes = "" #:: Stream.from(1).map(_.toString)
    val nameWithSuffixes = suffixes.map(suffix => s"$name$suffix")
    nameWithSuffixes.find(n => !reservedClassNames.contains(n))
      .map(_.toClassName)
  }
}
