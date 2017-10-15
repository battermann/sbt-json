package json2caseclass.implementation

import json2caseclass.model
import json2caseclass.model.Schema._
import json2caseclass.model.Types.Suffix

object NameTransformer {
  def apply(suffix: Suffix): model.NameTransformer = model.NameTransformer(
    NameTransformer.makeSafeCamelCaseCaseClassName(suffix),
    NameTransformer.makeSafeFieldName
  )

  def makeSafeCamelCaseCaseClassName(suffix: Suffix)(objectName: String): SchemaObjectName = {
    if (reservedWords.contains(objectName.toLowerCase) || scalaTypes.map(_.toLowerCase).contains(
      objectName.toLowerCase)) {
      s"${objectName.capitalize}$suffix".toSchemaObjectName
    } else {
      objectName
        .split("_")
        .map(_.capitalize)
        .map(_.toSchemaObjectName)
        .mkString
        .toSchemaObjectName
    }
  }

  def makeSafeFieldName(fieldName: String): SchemaFieldName = {
    if (reservedWords.contains(fieldName.toLowerCase)) {
      s"`$fieldName`".toSchemaFieldName
    } else {
      fieldName.toSchemaFieldName
    }
  }

  private val scalaTypes = Seq(
    "Boolean",
    "Byte",
    "Short",
    "Int",
    "Long",
    "Float",
    "Double",
    "Char",
    "String",
    "Seq",
    "Set",
    "Map",
    "Array",
    "Range",
    "Vector",
    "List",
    "Queue",
    "Stack",
    "Stream"
  )

  private val reservedWords = Seq(
    "abstract",
    "case",
    "catch",
    "class",
    "def",
    "do",
    "else",
    "extends",
    "false",
    "final",
    "finally",
    "for",
    "forSome",
    "if",
    "implicit",
    "import",
    "lazy",
    "macro",
    "match",
    "new",
    "null",
    "object",
    "override",
    "package",
    "private",
    "protected",
    "return",
    "sealed",
    "super",
    "this",
    "throw",
    "trait",
    "try",
    "true",
    "type",
    "val",
    "var",
    "while",
    "with",
    "yield"
  )
}
