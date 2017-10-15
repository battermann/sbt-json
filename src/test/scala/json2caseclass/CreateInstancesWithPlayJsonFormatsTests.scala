package json2caseclass

import json2caseclass.jsonmodels.{BingData, GoogleMapsApiData}
import org.scalatest._
import play.api.libs.json.Json

class CreateInstancesWithPlayJsonFormatsTests extends FlatSpec with Matchers {

  "With the generated source from google maps API it" should "be possible to create instance with play json" in {

    val jsonString =
      """{
        |  "results":[
        |    {
        |      "address_components":[
        |        {
        |          "long_name":"1600",
        |          "short_name":"1600",
        |          "types":[
        |            "street_number"
        |          ]
        |        },
        |        {
        |          "long_name":"Amphitheatre Pkwy",
        |          "short_name":"Amphitheatre Pkwy",
        |          "types":[
        |            "route"
        |          ]
        |        },
        |        {
        |          "long_name":"Mountain View",
        |          "short_name":"Mountain View",
        |          "types":[
        |            "locality",
        |            "political"
        |          ]
        |        },
        |        {
        |          "long_name":"Santa Clara",
        |          "short_name":"Santa Clara",
        |          "types":[
        |            "administrative_area_level_2",
        |            "political"
        |          ]
        |        },
        |        {
        |          "long_name":"California",
        |          "short_name":"CA",
        |          "types":[
        |            "administrative_area_level_1",
        |            "political"
        |          ]
        |        },
        |        {
        |          "long_name":"United States",
        |          "short_name":"US",
        |          "types":[
        |            "country",
        |            "political"
        |          ]
        |        },
        |        {
        |          "long_name":"94043",
        |          "short_name":"94043",
        |          "types":[
        |            "postal_code"
        |          ]
        |        }
        |      ],
        |      "formatted_address":"1600 Amphitheatre Pkwy, Mountain View, CA 94043, USA",
        |      "geometry":{
        |        "location":{
        |          "lat":37.42291810,
        |          "lng":-122.08542120
        |        },
        |        "location_type":"ROOFTOP",
        |        "viewport":{
        |          "northeast":{
        |            "lat":37.42426708029149,
        |            "lng":-122.0840722197085
        |          },
        |          "southwest":{
        |            "lat":37.42156911970850,
        |            "lng":-122.0867701802915
        |          }
        |        }
        |      },
        |      "types":[
        |        "street_address"
        |      ]
        |    }
        |  ],
        |  "status":"OK"
        |}""".stripMargin

    val mapsData = Json.parse(jsonString).as[GoogleMapsApiData]

    mapsData.results.head.address_components.head.long_name shouldEqual "1600"
  }

  "With the generated source from BING it" should "be possible to create instance with play json" in {

    val jsonString = """{"images":[{"startdate":"20170808","fullstartdate":"201708080700","enddate":"20170809","url":"/az/hprichbg/rb/AlaskaLynx_EN-US9313111559_1920x1080.jpg","urlbase":"/az/hprichbg/rb/AlaskaLynx_EN-US9313111559","copyright":"Canada lynx in Denali National Park, Alaska (© Design Pics Inc./Alamy)","copyrightlink":"http://www.bing.com/search?q=canada+lynx&form=hpcapt&filters=HpDate:%2220170808_0700%22","quiz":"/search?q=Bing+homepage+quiz&filters=WQOskey:%22HPQuiz_20170808_AlaskaLynx%22&FORM=HPQUIZ","wp":true,"hsh":"2449488dd2b169148b2745253aa48ee0","drk":1,"top":1,"bot":1,"hs":[]}],"tooltips":{"loading":"Loading...","previous":"Previous image","next":"Next image","walle":"This image is not available to download as wallpaper.","walls":"Download this image. Use of this image is restricted to wallpaper only."}}"""

    val bingData = Json.parse(jsonString).as[BingData]

    bingData.images.head.enddate shouldEqual "20170809"
  }
}
