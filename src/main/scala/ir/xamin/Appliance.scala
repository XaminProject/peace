package ir.xamin

import dispatch.json._
import sjson.json._
import JsonSerialization._

/** the Appliane
 */
case class Appliance(name: String, version: String, description: String, url: String, author: String, enabled:Boolean=false, tags:List[String])

/** companion object of Appliance to provide json conversion of Appliance
 */
object Appliance extends DefaultProtocol {
  implicit object ApplianceFormat extends Format[Appliance] {
    /** creates an Appliance from json string
     * @param json the js string
     * @return the Appliance
     */
    def reads(json: JsValue):Appliance = json match {
      case JsObject(m) =>
        Appliance(fromjson[String](m(JsString("name"))),
          fromjson[String](m(JsString("version"))),
          fromjson[String](m(JsString("description"))),
          fromjson[String](m(JsString("url"))),
          fromjson[String](m(JsString("author"))),
          fromjson[Boolean](m(JsString("enabled"))),
          fromjson[List[String]](m(JsString("tags")))
        )
      case _ => throw new RuntimeException("JsObject expected")
    }

    /** creates a json presentation of Appliance
     * @param p Appliance to be converted to json
     * @param the json
     */
    def writes(p: Appliance):JsValue =
      JsObject(List(
        (tojson("name").asInstanceOf[JsString], tojson(p.name)),
        (tojson("version").asInstanceOf[JsString], tojson(p.version)),
        (tojson("description").asInstanceOf[JsString], tojson(p.description)),
        (tojson("url").asInstanceOf[JsString], tojson(p.url)),
        (tojson("author").asInstanceOf[JsString], tojson(p.author)),
        (tojson("enabled").asInstanceOf[JsString], tojson(p.enabled)),
        (tojson("tags").asInstanceOf[JsString], tojson(p.tags)) ))
  }
}

// vim: set ts=4 sw=4 et:
