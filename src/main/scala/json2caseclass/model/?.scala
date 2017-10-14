package json2caseclass.model

import java.util.UUID

import cats.data.ReaderWriterStateT
import cats.implicits._
import json2caseclass.model.Types.{ErrorOr, ErrorRWSOr}

object ? {
  def <~[A](x: A): ErrorRWSOr[A] = x.pure[ErrorRWSOr]

  def <~[A](
    x: Either[CaseClassGenerationFailure, A]): ErrorRWSOr[A] = ReaderWriterStateT.lift[ErrorOr, Environment, Unit, UUID, A](
    x)
}
