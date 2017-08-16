package sbtjson

import j2cgen.models.CaseClassGenerationFailure

sealed trait SbtJsonFailure
case class CaseClassSourceGenFailure(err: CaseClassGenerationFailure) extends SbtJsonFailure
case class HttpTimeout(exception: Throwable) extends SbtJsonFailure
case class NetworkFailure(exception: Throwable) extends SbtJsonFailure