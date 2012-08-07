package ir.xamin.providers

import ir.xamin.packet.receive.MarketInstall
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.packet.IQ
import org.jivesoftware.smack.provider.IQProvider
import org.xmlpull.v1.XmlPullParser

/** this class parses received xml stanza as an MarketInstall packet
 */
class MarketInstallProvider extends IQProvider {
  /** parse the received stanza
   * @param parser the XmlPullParser of received packet
   * @return the MarketInstall representation of packet
   */
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
          case _ => Unit
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
