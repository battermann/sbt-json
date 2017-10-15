package json2caseclass.model

import java.util.UUID

import cats.data._
import play.api.libs.json.JsValue
import shapeless.tag
import shapeless.tag.@@

object Types {

  trait SuffixTag
  type Suffix = String @@ SuffixTag

  implicit class ToSuffix(name: String) {
    def toSuffix: Suffix = tag[SuffixTag][String](name)
  }

  type JsValueFilter = JsValue => Boolean

  trait RootTypeNameTag

  type RootTypeName = String @@ RootTypeNameTag

  implicit class ToRootTypeName(name: String) {
    def toRootTypeName: RootTypeName = tag[RootTypeNameTag][String](name)
  }

  trait JsonStringTag
  type JsonString = String @@ JsonStringTag

  implicit class ToJsonString(json: String) {
    def toJsonString: JsonString = tag[JsonStringTag][String](json)
  }

  trait CaseClassSourceTag
  type CaseClassSource = String @@ CaseClassSourceTag

  implicit class ToCaseClassSource(caseClassSource: String) {
    def toCaseClassSource: CaseClassSource = tag[CaseClassSourceTag][String](caseClassSource)
  }

  type Interpreter = Seq[CaseClass] => CaseClassSource

  type ErrorOr[A] = Either[CaseClassGenerationFailure, A]
  type ErrorRWSOr[A] = ReaderWriterStateT[ErrorOr, Environment, Unit, Int, A]
}
