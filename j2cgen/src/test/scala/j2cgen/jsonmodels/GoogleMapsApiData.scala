package j2cgen.jsonmodels

import play.api.libs.json.Json

case class GoogleMapsApiData(
                              results: List[Results],
                              status: String
                            )

case class Results(
                    address_components: List[Address_components],
                    formatted_address: String,
                    geometry: Geometry,
                    types: List[String]
                  )

case class Address_components(
                               long_name: String,
                               short_name: String,
                               types: List[String]
                             )

case class Geometry(
                     location: Location,
                     location_type: String,
                     viewport: Viewport
                   )

case class Location(
                     lat: Double,
                     lng: Double
                   )

case class Viewport(
                     northeast: Location,
                     southwest: Location
                   )



object GoogleMapsApiData {
  implicit val formatLocation = Json.format[Location]
  implicit val formatViewport = Json.format[Viewport]
  implicit val formatGeometry = Json.format[Geometry]
  implicit val formatAddress_components = Json.format[Address_components]
  implicit val formatResults = Json.format[Results]
  implicit val formatGoogleMapsApiData = Json.format[GoogleMapsApiData]
}

