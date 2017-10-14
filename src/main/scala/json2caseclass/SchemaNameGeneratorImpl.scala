package json2caseclass

import json2caseclass.model.Schema._
import json2caseclass.model.suffix._

object SchemaNameGeneratorImpl {

  def generateCaseClassName(suffix: Suffix)(objectName: String): SchemaObjectName = {
    if (reservedWords.contains(objectName.toLowerCase) || scalaTypes.map(_.toLowerCase).contains(objectName.toLowerCase)) {
      s"${objectName.capitalize}$suffix".toSchemaObjectName
    } else {
      objectName
        .capitalize
        .toSchemaObjectName
    }
  }

  def generateFieldName(fieldName: String): SchemaFieldName = {
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
