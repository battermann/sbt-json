package json2caseclass.jsonmodels

case class Images(
    startdate: String,
    fullstartdate: String,
    enddate: String,
    url: String,
    urlbase: String,
    copyright: String,
    copyrightlink: String,
    quiz: String,
    wp: Boolean,
    hsh: String,
    drk: Double,
    top: Double,
    bot: Double
)
case class Tooltips(
    loading: String,
    previous: String,
    next: String,
    walle: String,
    walls: String
)
case class BingData(
    images: List[Images],
    tooltips: Tooltips
)

object BingData {
  import play.api.libs.json.Json

  implicit val imagesFormats   = Json.format[Images]
  implicit val tooltipsFormats = Json.format[Tooltips]
  implicit val bingDataFormats = Json.format[BingData]
}
