package sbtjson

import j2cgen.models.CaseClassGenerationFailure

sealed trait SbtJsonFailure
case class CaseClassSourceGenFailure(fileNameOrUrl: String, err: CaseClassGenerationFailure) extends SbtJsonFailure
case class HttpTimeout(url: String, exception: Throwable) extends SbtJsonFailure
case class NetworkFailure(url: String, exception: Throwable) extends SbtJsonFailure