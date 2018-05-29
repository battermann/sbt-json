package json2caseclass.implementation

import java.util.regex.Pattern

import json2caseclass.model
import json2caseclass.model.Schema._
import json2caseclass.model.Types.Suffix

object NameTransformer {
  def apply(suffix: Suffix): model.NameTransformer = model.NameTransformer(
    NameTransformer.makeSafeCamelCaseCaseClassName(suffix),
    NameTransformer.makeSafeFieldName
  )

  def makeSafeFieldName(fieldName: String): SchemaFieldName = {
    if (reservedWords.contains(fieldName.toLowerCase) || containsInvalidChars(fieldName)) {
      s"`$fieldName`".toSchemaFieldName
    } else {
      fieldName.toSchemaFieldName
    }
  }

  def makeSafeCamelCaseCaseClassName(suffix: Suffix)(objectName: String): SchemaObjectName = {
    normalizeName(suffix)(objectName).toSchemaObjectName
  }

  def normalizeName(suffix: Suffix)(objectName: String): String = {
    if (reservedWords.contains(objectName.toLowerCase) || scalaTypes
          .map(_.toLowerCase)
          .contains(objectName.toLowerCase)) {
      s"${objectName.capitalize}$suffix"
    } else {
      objectName
        .replaceFirst("^[^a-zA-Z]", "")
        .split("[^a-zA-Z0-9]")
        .filter(_.nonEmpty)
        .map(_.capitalize)
        .mkString
    }
  }

  def containsInvalidChars(name: String): Boolean = {
    val invalidChars     = "[^a-zA-Z0-9_]"
    val invalidFirstChar = "^[^a-zA-Z_]"
    Pattern.compile(invalidChars).matcher(name).find() ||
    Pattern.compile(invalidFirstChar).matcher(name).find()
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
