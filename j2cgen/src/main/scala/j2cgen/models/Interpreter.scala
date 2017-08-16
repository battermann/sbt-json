package j2cgen.models

import j2cgen.models.caseClassSource.CaseClassSource

object Interpreter {
  type Interpreter = Seq[CaseClass] => CaseClassSource
}
