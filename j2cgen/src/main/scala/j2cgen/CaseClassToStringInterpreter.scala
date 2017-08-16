package j2cgen

import j2cgen.models._
import j2cgen.models.caseClassSource._
import j2cgen.models.caseClassSource.CaseClassSource

object CaseClassToStringInterpreter {

  def interpretWithPlayJsonFormats(caseClasses: Seq[CaseClass]): CaseClassSource = {
    s"""${interpret(caseClasses)}
       |${createCompanionObjectWithPlayJsonFormats(caseClasses)}
       |"""
      .stripMargin
      .toCaseClassSource
  }

  def interpret(caseClasses: Seq[CaseClass]): CaseClassSource = {
    val caseClassSource = caseClasses
      .map(interpret)
      .mkString("\n\n") + "\n"
    caseClassSource.toCaseClassSource
  }

  private def createCompanionObjectWithPlayJsonFormats(caseClasses: Seq[CaseClass]): String = {
    val formats =
      caseClasses
        .reverse
        .map(o => s"  implicit val format${o.name} = Json.format[${o.name}]")
        .mkString("\n")

    s"""object ${caseClasses.head.name} {
       |  import play.api.libs.json.Json
       |
       |$formats
       |}""".stripMargin
  }

  private def interpret(caseClass: CaseClass): String = {
    val CaseClass(_, name, fields) = caseClass

    val members = fields.map { case (fieldName, fieldType) =>
      s"$fieldName: ${typeName(fieldType)}"
    }

    s"case class $name(\n  ${members.mkString(",\n  ")}\n)"
  }

  private def typeName(scalaType: ScalaType): String = {
    scalaType match {
      case ScalaOption(optionType) => s"Option[${typeName(optionType)}]"
      case ScalaString => "String"
      case ScalaDouble => "Double"
      case ScalaBoolean => "Boolean"
      case ScalaSeq(seqType) => s"Seq[${typeName(seqType)}]"
      case ScalaObject(_, name) => name
    }
  }
}

