package json2caseclass.model

import cats.data.ReaderWriterStateT
import cats.implicits._
import json2caseclass.model.Types.{ErrorOr, ErrorRWSOr}

object ? {
  def <~[A](x: Either[CaseClassGenerationFailure, A]): ErrorRWSOr[A] =
    ReaderWriterStateT.liftF[ErrorOr, Environment, Unit, Int, A](x)
}
