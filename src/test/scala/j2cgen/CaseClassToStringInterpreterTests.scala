package j2cgen

import java.util.UUID

import j2cgen.models.CaseClass._
import j2cgen.models.ScalaType._
import j2cgen.models.{CaseClass, ScalaObject}
import org.scalatest._
import CaseClassToStringInterpreter._
import j2cgen.models.Interpreter.Interpreter

class CaseClassToStringInterpreterTests extends FlatSpec with Matchers {

  "Sequence of case classes" should "be sorted by dependency for play-json formats" in {
    val model1Id = UUID.randomUUID
    val model2Id = UUID.randomUUID
    val model3Id = UUID.randomUUID
    val model4Id = UUID.randomUUID
    val model5Id = UUID.randomUUID

    val caseClasses = Seq(
      CaseClass(model1Id.toClassId, "Model1".toClassName, Seq("model3".toClassFieldName -> ScalaObject(model3Id.toScalaObjectId, "Model3".toScalaObjectName))),
      CaseClass(
        model2Id.toClassId,
        "Model2".toClassName,
        Seq(
          "model1".toClassFieldName -> ScalaObject(model1Id.toScalaObjectId, "Model1".toScalaObjectName),
          "model4".toClassFieldName -> ScalaObject(model4Id.toScalaObjectId, "Model4".toScalaObjectName)
        )
      ),
      CaseClass(model3Id.toClassId, "Model3".toClassName, Seq("model4".toClassFieldName -> ScalaObject(model4Id.toScalaObjectId, "Model4".toScalaObjectName))),
      CaseClass(model4Id.toClassId, "Model4".toClassName, Nil),
      CaseClass(model5Id.toClassId, "Model5".toClassName, Seq("model3".toClassFieldName -> ScalaObject(model3Id.toScalaObjectId, "Model3".toScalaObjectName)))
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

