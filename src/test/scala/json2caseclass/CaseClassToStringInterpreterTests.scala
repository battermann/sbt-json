package json2caseclass

import json2caseclass.implementation.CaseClassToStringInterpreter._
import json2caseclass.model.CaseClass._
import json2caseclass.model.ScalaType._
import json2caseclass.model.{CaseClass, ScalaObject}
import org.scalatest._

class CaseClassToStringInterpreterTests extends FlatSpec with Matchers {

  "Sequence of case classes" should "be sorted by dependency for play-json formats" in {
    val caseClasses = Seq(
      CaseClass(0, "Model1".toClassName, Seq("model3".toClassFieldName -> ScalaObject(2, "Model3".toScalaObjectName))),
      CaseClass(
        1,
        "Model2".toClassName,
        Seq(
          "model1".toClassFieldName -> ScalaObject(0, "Model1".toScalaObjectName),
          "model4".toClassFieldName -> ScalaObject(3, "Model4".toScalaObjectName)
        )
      ),
      CaseClass(2, "Model3".toClassName, Seq("model4".toClassFieldName -> ScalaObject(3, "Model4".toScalaObjectName))),
      CaseClass(3, "Model4".toClassName, Nil),
      CaseClass(4, "Model5".toClassName, Seq("model3".toClassFieldName -> ScalaObject(2, "Model3".toScalaObjectName)))
    )

    val expected = """case class Model1(
                     |  model3: Model3
                     |)
                     |
                     |case class Model2(
                     |  model1: Model1,
                     |  model4: Model4
                     |)
                     |
                     |case class Model3(
                     |  model4: Model4
                     |)
                     |
                     |case class Model4(
                     |)
                     |
                     |case class Model5(
                     |  model3: Model3
                     |)
                     |
                     |object Model1 {
                     |  import play.api.libs.json.Json
                     |
                     |  implicit val formatModel4 = Json.format[Model4]
                     |  implicit val formatModel3 = Json.format[Model3]
                     |  implicit val formatModel5 = Json.format[Model5]
                     |  implicit val formatModel1 = Json.format[Model1]
                     |  implicit val formatModel2 = Json.format[Model2]
                     |}
                     |""".stripMargin

    val actual = plainCaseClasses.withPlayJsonFormats(caseClasses)

    actual shouldEqual expected
  }
}
