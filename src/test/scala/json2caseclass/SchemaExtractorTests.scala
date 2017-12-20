package json2caseclass

import json2caseclass.implementation.SchemaExtractor
import json2caseclass.model._
import json2caseclass.model.Types._
import json2caseclass.model.Schema._
import org.scalatest._
import play.api.libs.json.Json
import cats.implicits._

class SchemaExtractorTests extends FlatSpec with Matchers {

  "Schema" should "be correct" in {
    val json = """{
                 |  "metrics":[
                 |    {
                 |      "tags":[
                 |        {
                 |          "value":"2xx"
                 |        }
                 |      ]
                 |    },
                 |    {
                 |      "tags":[
                 |        {
                 |          "value":"3xx"
                 |        }
                 |      ]
                 |    }
                 |  ]
                 |}""".stripMargin

    val env = Environment(
      nameTransformer = implementation.NameTransformer("Model".toSuffix)
    )

    val expected = Right(
      SchemaObject(
        5,
        "Alb".toSchemaObjectName,
        List(
          (
            "metrics".toSchemaFieldName,
            SchemaArray(
              SchemaObject(
                2,
                "Metrics".toSchemaObjectName,
                List(("tags".toSchemaFieldName, SchemaArray(SchemaObject(1, "Tags".toSchemaObjectName, List(("value".toSchemaFieldName, SchemaString))))))
              )
            )
          )
        )
      )
    )

    val actual = SchemaExtractor.extractSchemaFromJsonRoot("Alb".toRootTypeName, Json.parse(json)).runA(env, 1)

    actual shouldEqual expected
  }

  "Schema" should "be correct with optionals" in {
    val json = """[
                 |  {
                 |    "name": "RequestCount"
                 |  },
                 |  {
                 |    "name": "HTTPCode_Target_2XX_Count",
                 |    "tags": [
                 |      {
                 |        "value": "2xx"
                 |      }
                 |    ]
                 |  },
                 |  {
                 |    "name": "HTTPCode_Target_3XX_Count",
                 |    "tags": [
                 |      {
                 |        "value": "3xx"
                 |      }
                 |    ]
                 |  }
                 |]""".stripMargin

    val env = Environment(
      nameTransformer = implementation.NameTransformer("Model".toSuffix)
    )

    val expected = Right(
      SchemaArray(
        SchemaObject(
          6,
          "Alb".toSchemaObjectName,
          List(
            ("name".toSchemaFieldName, SchemaString),
            ("tags".toSchemaFieldName, SchemaOption(SchemaArray(SchemaObject(2, "Tags".toSchemaObjectName, List(("value".toSchemaFieldName, SchemaString))))))
          )
        )
      )
    )


    val actual = SchemaExtractor.extractSchemaFromJsonRoot("Alb".toRootTypeName, Json.parse(json)).runA(env, 1)

    actual shouldEqual expected
  }

}

