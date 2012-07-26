package ir.xamin.providers

import ir.xamin.packet.MarketInstall
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.packet.IQ
import org.jivesoftware.smack.provider.IQProvider
import org.xmlpull.v1.XmlPullParser

class MarketInstallProvider extends IQProvider {
  def parseIQ(parser: XmlPullParser): IQ = {
    val marketInstall = new MarketInstall
    marketInstall setArchipel parser.getAttributeValue("", "to")
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
        if(parser.getName == MarketInstallProvider.element)
          remaining = false
      }
    }
    marketInstall setInstall appliances
    marketInstall
  }
}

object MarketInstallProvider {
  val namespace = "market:xamin"
  val element = "install"
}

// vim: set ts=4 sw=4 et:
