package ir.xamin.providers

import ir.xamin.packet.receive.ApplianceEnable
import org.jivesoftware.smack.packet.IQ
import org.jivesoftware.smack.provider.IQProvider
import org.xmlpull.v1.XmlPullParser

/** this class parses xml stream to provide an ApplianceEnable packet
 */
class ApplianceEnableProvider extends IQProvider {
  /** parse the received xml
   * @param parser the XmlPullParser object of received stanza
   * @return the ApplianceEnable presentation of packet
   */
  def parseIQ(parser: XmlPullParser): IQ = {
    val applianceEnable = new ApplianceEnable
    var remaining = true
    while(remaining) {
      val eventType = parser.next()
      if(eventType == XmlPullParser.START_TAG) {
        val name = parser.getName()
        name match {
          case "name" => applianceEnable.setName(parser.nextText())
          case "version" => applianceEnable.setVersion(parser.nextText())
          case _ => Unit
        }
      } else if(eventType == XmlPullParser.END_TAG) {
        if(parser.getName() == ApplianceSetProvider.element)
          remaining = false
      }
    }
    return applianceEnable
  }
}

object ApplianceEnableProvider {
  val namespace = "appliance:enable:xamin"
  val element = "appliance"
}

// vim: set ts=4 sw=4 et:
