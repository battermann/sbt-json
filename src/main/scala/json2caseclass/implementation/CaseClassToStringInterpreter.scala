package json2caseclass.implementation

import json2caseclass.model.Types.{Interpreter, _}
import json2caseclass.model.{ScalaType, _}

object CaseClassToStringInterpreter {

  implicit class InterpreterOptions(interpreter: Interpreter) {
    def withPlayJsonFormats =
      CaseClassToStringInterpreter.withPlayJsonFormats(interpreter)
  }

  def withPlayJsonFormats: Interpreter => Interpreter = { interpreter =>
    { caseClasses =>
      s"""${interpreter(caseClasses)}
           |${createCompanionObjectWithPlayJsonFormats(caseClasses)}
           |""".stripMargin.toCaseClassSource
    }
  }

  def plainCaseClasses: Interpreter = { caseClasses =>
    {
      val caseClassSource = caseClasses
        .map(interpret)
        .mkString("\n\n") + "\n"
      caseClassSource.toCaseClassSource
    }
  }

  private def sortForPlayJsonFormats(caseClasses: Seq[CaseClass]): Seq[CaseClass] = {
    def sort(unordered: Seq[CaseClass], ordered: Seq[CaseClass]): Seq[CaseClass] = {
      unordered match {
        case h :: t =>
          if (hasNoUnresolvedDependencies(ordered, h)) {
            sort(t, ordered ++ Seq(h))
          } else {
            sort(t ++ Seq(h), ordered)
          }
        case Nil =>
          ordered
      }
    }

    sort(caseClasses, Nil)
  }

  private def hasNoUnresolvedDependencies(alreadyDefined: Seq[CaseClass], caseClass: CaseClass) = {
    val CaseClass(_, _, fields) = caseClass

    fields.forall {
      case o @ (_, ScalaObject(_, _)) =>
        typeExistsInUnresolvedDeps(alreadyDefined, o._2)
      case (_, ScalaSeq(t)) =>
        typeExistsInUnresolvedDeps(alreadyDefined, t)
      case (_, ScalaOption(t)) =>
        typeExistsInUnresolvedDeps(alreadyDefined, t)
      case _ => true
    }
  }

  private def typeExistsInUnresolvedDeps(alreadyDefined: Seq[CaseClass], scalaType: ScalaType): Boolean = {
    scalaType match {
      case ScalaOption(t) =>
        typeExistsInUnresolvedDeps(alreadyDefined, t)
      case ScalaSeq(t) =>
        typeExistsInUnresolvedDeps(alreadyDefined, t)
      case ScalaObject(_, dependencyName) =>
        alreadyDefined.exists {
          case CaseClass(_, name, _) => name == dependencyName
        }
      case _ => true
    }
  }

  private def createCompanionObjectWithPlayJsonFormats(caseClasses: Seq[CaseClass]): String = {
    val formats =
      sortForPlayJsonFormats(caseClasses)
        .map(caseClass => s"  implicit val format${caseClass.name} = Json.format[${caseClass.name}]")
        .mkString("\n")

    s"""object ${caseClasses.head.name} {
       |  import play.api.libs.json.Json
       |
       |$formats
       |}""".stripMargin
  }

  private def interpret(caseClass: CaseClass): String = {
    val CaseClass(_, name, fields) = caseClass

    val members = fields.map {
      case (fieldName, fieldType) =>
        s"$fieldName: ${typeName(fieldType)}"
    }

    s"case class $name(${members.map(m => s"\n  $m").mkString(",")}\n)"
  }

  private def typeName(scalaType: ScalaType): String = {
    scalaType match {
      case ScalaOption(optionType) => s"Option[${typeName(optionType)}]"
      case ScalaString             => "String"
      case ScalaDouble             => "Double"
      case ScalaBoolean            => "Boolean"
      case ScalaSeq(seqType)       => s"Seq[${typeName(seqType)}]"
      case ScalaObject(_, name)    => name
    }
  }
}
