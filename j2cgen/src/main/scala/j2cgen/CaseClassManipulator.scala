package j2cgen

import java.util.UUID

import j2cgen.models.CaseClass._
import j2cgen.models.ScalaType._
import j2cgen.models._

object CaseClassManipulator {
  def rename(makeUnique: (Set[String], ClassName) => Option[ClassName], caseClasses: Seq[CaseClass]): Seq[CaseClass] = {
    val alternativeNames = findAlternativeNames(makeUnique, caseClasses)
    rename(caseClasses, alternativeNames)
  }

  def addOptionals(caseClasses: Seq[CaseClass], optionals: Seq[(ClassName, ClassFieldName)]): Seq[CaseClass] = {
    caseClasses.map { case o@CaseClass(_, name, fields) =>
      val withOptionals =
        fields.map {
          case (fName, scalaType) if optionals.contains(name -> fName) =>
            (fName, ScalaOption(scalaType))
          case other => other
        }
      o.copy(fields = withOptionals)
    }
  }

  private def findAlternativeNames(makeUnique: (Set[String], ClassName) => Option[ClassName], caseClasses: Seq[CaseClass]): Map[UUID, ClassName] = {
    def alternativeNamesRec(
      ccs: Seq[CaseClass],
      alreadyReservedNames: Set[String],
      acc: Map[UUID, ClassName]): Map[UUID, ClassName] = {

      ccs match {
        case Nil => acc
        case CaseClass(id, name, _) :: tail =>
          makeUnique(alreadyReservedNames, name) match {
            case Some(uniqueName) =>
              alternativeNamesRec(tail, alreadyReservedNames + uniqueName, acc + (id -> uniqueName))
            case None =>
              alternativeNamesRec(tail, alreadyReservedNames + name, acc)
          }
      }
    }

    alternativeNamesRec(caseClasses, Set(), Map())
  }

  private def rename(caseClasses: Seq[CaseClass], alternativeNames: Map[UUID, ClassName]): Seq[CaseClass] = {
    def renameFieldTypes(fields: Seq[(ClassFieldName, ScalaType)]) = {
      fields.map {
        case f@(fName, ScalaObject(id, _)) =>
          alternativeNames
            .get(id)
            .map(altName => (fName, ScalaObject(id, altName.toScalaObjectName)))
            .getOrElse(f)
        case other => other
      }
    }

    caseClasses.map { case o@CaseClass(id, _, fields) =>
      alternativeNames
        .get(id)
        .map(altName => o.copy(name = altName))
        .getOrElse(o)
        .copy(fields = renameFieldTypes(fields))
    }
  }
}
