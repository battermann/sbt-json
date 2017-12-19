package sbtjson

import json2caseclass.model._

object ErrorMessages {
  def mkMessage(error: SbtJsonFailure): String = {
    error match {
      case NetworkFailure(url ,ex) =>
        s"""An network error occurred while trying to retrieve a JSON document from a URL: '$url'
           |${ex.toString}""".stripMargin
      case HttpTimeout(url, ex) =>
        s"""An time out error occurred while trying to retrieve a JSON document from a URL: '$url'
           |${ex.toString}""".stripMargin
      case CaseClassSourceGenFailure(fileNameOrUrl, generationFailure) =>
        generationFailure match {
          case ValueIsNull(name) =>
            s"""An error occurred while extracting the schema from the JSON document from the file or URL: '$fileNameOrUrl#
               |A value '$name# is either null or the value is an array containing a null value.
               |There might be important type information missing because the type of a null value cannot be analyzed.
               |To ignore null values explicitly set the sbt-json option 'jsValueFilter' to ignore null:
               |'jsValueFilter := allJsValues.exceptNullValues'""".stripMargin
          case ArrayEmpty(name) =>
            s"""An error occurred while extracting the schema from the JSON document from the file or URL: '$fileNameOrUrl'
               |The value '$name' is an empty array.
               |There might be important type information missing because the type of an empty array cannot be analyzed.
               |To ignore empty arrays explicitly set the sbt-json option 'jsValueFilter' to ignore empty arrays:
               |'jsValueFilter := allJsValues.exceptEmptyArrays'""".stripMargin
          case ArrayTypeNotConsistent(name) =>
            s"""An error occurred while extracting the schema from the JSON document from the file or URL: '$fileNameOrUrl'
               |The type of value '$name' is not consistent. The schema cannot be inferred.""".stripMargin
          case JsonParseFailure(ex) =>
            s"""An error occurred while trying to parse the JSON document from the file or URL: '$fileNameOrUrl'
               |${ex.getMessage}""".stripMargin
          case InvalidRoot | JsonContainsNoObjects =>
            s"""An error occurred while trying to extract the schema from the JSON document from the file or URL: '$fileNameOrUrl'
               |The root value of the JSON document is invalid. It must be either an object or an array of objects.""".stripMargin
        }
    }
  }
}
