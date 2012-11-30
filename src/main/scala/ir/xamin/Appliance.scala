package ir.xamin

import dispatch.json._
import sjson.json._
import JsonSerialization._

/** the Appliane
 */
case class Appliance(name:String, version:String, description:String, url:String,
  author:String, enabled:Boolean, tags:List[String], cpu:Int, memory:Int, storage:Int,
  category:String, images:List[Map[String, String]], icon:String, creation:Long, home:String,
  payment:PaymentPolicy)

/** companion object of Appliance to provide json conversion of Appliance
 */
object Appliance extends DefaultProtocol {
  implicit object ApplianceFormat extends Format[Appliance] {
    /** creates an Appliance from json string
     * @param json the js string
     * @return the Appliance
     */
    def reads(json: JsValue):Appliance = json match {
      case JsObject(m) => {
        var pp:PaymentPolicy = null
        val ppTmp = m.get(JsString("payment"))
        if (!ppTmp.isEmpty) {
          pp = fromjson[PaymentPolicy](ppTmp.get)
        }
        try {
          Appliance(fromjson[String](m(JsString("name"))),
            fromjson[String](m(JsString("version"))),
            fromjson[String](m(JsString("description"))),
            fromjson[String](m(JsString("url"))),
            fromjson[String](m(JsString("author"))),
            fromjson[Boolean](m(JsString("enabled"))),
            fromjson[List[String]](m(JsString("tags"))),
            fromjson[Int](m.getOrElse(JsString("cpu"), JsNumber(1))),
            fromjson[Int](m.getOrElse(JsString("memory"), JsNumber(64))),
            fromjson[Int](m.getOrElse(JsString("storage"), JsNumber(8))),
            fromjson[String](m.getOrElse(JsString("category"), JsString("others"))),
            fromjson[List[Map[String, String]]](m.getOrElse(JsString("images"), JsArray(List[JsValue]()))),
            fromjson[String](m.getOrElse(JsString("icon"), JsString(""))),
            fromjson[Long](m.getOrElse(JsString("creation"), JsNumber(0))),
            fromjson[String](m.getOrElse(JsString("home"), JsString(""))),
            pp
          )
        } catch {
          case e:Exception => {
            Console.err.println(e.printStackTrace)
            Console.err.println(m)
            throw new RuntimeException("Invalid Appliance json")
          }
        }
      }
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
        (tojson("tags").asInstanceOf[JsString], tojson(p.tags)),
        (tojson("cpu").asInstanceOf[JsString], tojson(p.cpu)),
        (tojson("memory").asInstanceOf[JsString], tojson(p.memory)),
        (tojson("storage").asInstanceOf[JsString], tojson(p.storage)),
        (tojson("category").asInstanceOf[JsString], tojson(p.category)),
        (tojson("images").asInstanceOf[JsString], tojson(p.images)),
        (tojson("icon").asInstanceOf[JsString], tojson(p.icon)),
        (tojson("creation").asInstanceOf[JsString], tojson(p.creation)),
        (tojson("home").asInstanceOf[JsString], tojson(p.home)),
        (tojson("payment").asInstanceOf[JsString], tojson[PaymentPolicy](p.payment))
      ))
  }
}

// vim: set ts=4 sw=4 et:
