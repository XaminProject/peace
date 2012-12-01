package ir.xamin

import dispatch.json._
import sjson.json._
import JsonSerialization._

/** the PaymentPolicy
 */
case class PaymentPolicy(stock:Double, off:List[Off])

/** companion object of PaymentPolicy to provide implict json conversion
 */
object PaymentPolicy extends DefaultProtocol {
  implicit object PaymentPolicyFormat extends Format[PaymentPolicy] {
    /** creates a PaymentPolicy from json string
     * @param json the json parsed as JsValue
     * @return the parsed PaymentPolicy
     */
    def reads(json: JsValue):PaymentPolicy = json match {
      case JsObject(m) =>
        PaymentPolicy(fromjson[Double](m(JsString("stock"))),
          fromjson[List[Off]](m(JsString("off")))
        )
      case JsNull => null
      case _ =>
        throw new RuntimeException("JsObject expected")
    }

    /** creates a json presentation of PaymentPolicy
     * @param p PaymentPolicy to be converted to json
     * @param the json
     */
    def writes(p:PaymentPolicy):JsValue =
      if(p==null)
        return JsNull
      else
        return JsObject(List(
          (tojson("stock").asInstanceOf[JsString], tojson(p.stock)),
          (tojson("off").asInstanceOf[JsString], tojson(p.off))
        ))
  }
}

// vim: set ts=4 sw=4 et:
