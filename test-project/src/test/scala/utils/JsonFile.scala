package scala.utils

import scala.io.Source

object JsonFile {
  def readeJsonFile(fileName: String): String = {
    Source.fromFile(s"src/main/resources/json/$fileName.json").getLines.mkString
  }
}
