package json2caseclass.model

import json2caseclass.model.caseClassSource.CaseClassSource

object Interpreter {
  type Interpreter = Seq[CaseClass] => CaseClassSource
}
