package foo

import models.json.foo._
import models.json.hpimagearchive._

import play.api.libs.json._

object Program extends App {
  val jsonString =
    """{"images":[{"startdate":"20170816","fullstartdate":"201708160700","enddate":"20170817","url":"/az/hprichbg/rb/AvalancheCreek_EN-US9065774002_1920x1080.jpg","urlbase":"/az/hprichbg/rb/AvalancheCreek_EN-US9065774002","copyright":"Avalanche Creek in Glacier National Park, Montana (© Ian Shive/Tandem Stills + Motion)","copyrightlink":"http://www.bing.com/search?q=glacier+national+park&form=hpcapt&filters=HpDate:%2220170816_0700%22","quiz":"/search?q=Bing+homepage+quiz&filters=WQOskey:%22HPQuiz_20170816_AvalancheCreek%22&FORM=HPQUIZ","wp":true,"hsh":"43ac32e637a3920fde6b5eb901d2106a","drk":1,"top":1,"bot":1,"hs":[]}],"tooltips":{"loading":"Loading...","previous":"Previous image","next":"Next image","walle":"This image is not available to download as wallpaper.","walls":"Download this image. Use of this image is restricted to wallpaper only."}}"""

  val bingData = Json.parse(jsonString).as[HPImageArchive]

  println(bingData.images.head.url)
}