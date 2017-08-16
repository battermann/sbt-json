package j2cgen

import j2cgen.models.json.JsonString
import j2cgen.models.{CaseClassGenerationFailure, JsonParseFailure}
import play.api.libs.json.{JsValue, Json}

import scala.util.{Failure, Success, Try}

object JsonParser {
  def parse(jsonString: JsonString): Either[CaseClassGenerationFailure, JsValue] = {
    Try(Json.parse(jsonString)) match {
      case Success(jsValue) =>
        Right(jsValue)
      case Failure(e) =>
        Left(JsonParseFailure(e))
    }
  }
}
