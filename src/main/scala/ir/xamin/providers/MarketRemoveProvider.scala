package ir.xamin.providers

import ir.xamin.packet.receive.MarketRemove
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.packet.IQ
import org.jivesoftware.smack.provider.IQProvider
import org.xmlpull.v1.XmlPullParser

class MarketRemoveProvider extends IQProvider {
  def parseIQ(parser: XmlPullParser): IQ = {
    val marketRemove = new MarketRemove
    marketRemove setArchipel parser.getAttributeValue("", "from")
    val appliances = MutableList[Tuple2[String, String]]()
    var remaining = true
    while(remaining) {
      val eventType = parser.next()
      if(eventType == XmlPullParser.START_TAG) {
        val tagName = parser.getName()
        tagName match {
          case "appliance" => {
            val version = parser.getAttributeValue("", "version")
            appliances += Tuple2[String, String](parser.nextText(), version)
          }
        }
      } else if(eventType == XmlPullParser.END_TAG) {
        if(parser.getName == MarketRemoveProvider.element)
          remaining = false
      }
    }
    marketRemove setRemove appliances
    marketRemove
  }
}

object MarketRemoveProvider {
  val namespace = "market:xamin"
  val element = "remove"
}

// vim: set ts=4 sw=4 et:
