package ir.xamin

import dispatch.json._
import sjson.json._
import JsonSerialization._

/** the PaymentPolicy
 */
case class Off(percent:Double, incase:Map[String, String])

/** companion object of Off to provide implict json conversion
 */
object Off extends DefaultProtocol {
  implicit object OffFormat extends Format[Off] {
    /** creates an instance of Off from json
     * @param json the json parsed as JsValue
     * @return the parsed PaymentPolicy
     */
    def reads(json: JsValue):Off = json match {
      case JsObject(m) =>
        Off(fromjson[Double](m(JsString("percent"))),
          fromjson[Map[String, String]](m(JsString("incase")))
        )
      case _ => throw new RuntimeException("JsObject expected")
    }

    /** creates a json presentation of PaymentPolicy
     * @param p PaymentPolicy to be converted to json
     * @param the json
     */
    def writes(p:Off):JsValue =
      JsObject(List(
        (tojson("percent").asInstanceOf[JsString], tojson(p.percent)),
        (tojson("incase").asInstanceOf[JsString], tojson(p.incase))
      ))
  }
}

// vim: set ts=4 sw=4 et:
