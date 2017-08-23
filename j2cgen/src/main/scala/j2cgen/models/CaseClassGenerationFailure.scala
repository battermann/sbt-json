package j2cgen.models

sealed trait CaseClassGenerationFailure
case class ValueIsNull(name: String) extends CaseClassGenerationFailure
case class JsonParseFailure(exception: Throwable) extends CaseClassGenerationFailure
case object InvalidRoot extends CaseClassGenerationFailure
case class ArrayTypeNotConsistent(name: String) extends CaseClassGenerationFailure
case class ArrayEmpty(name: String) extends CaseClassGenerationFailure
case object JsonContainsNoObjects extends CaseClassGenerationFailure
