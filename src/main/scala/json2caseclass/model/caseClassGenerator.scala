package json2caseclass.model

import java.util.UUID

import cats._
import cats.data._
import cats.implicits._

object caseClassGenerator {
  type ErrorOr[A] = Either[CaseClassGenerationFailure, A]
  type ErrorRWSOr[A] = ReaderWriterStateT[ErrorOr, Environment, Unit, UUID, A]
}
