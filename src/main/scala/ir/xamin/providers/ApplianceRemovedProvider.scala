package ir.xamin.providers

import ir.xamin.packet.receive.ApplianceRemoved
import org.jivesoftware.smack.packet.IQ
import org.jivesoftware.smack.provider.IQProvider
import org.xmlpull.v1.XmlPullParser

/** this class parses xml stream to provide an ApplianceRemoved packet
 */
class ApplianceRemovedProvider extends IQProvider {
  /** parse the received xml
   * @param parser the XmlPullParser object of received stanza
   * @return the ApplianceRemoved presentation of packet
   */
  def parseIQ(parser: XmlPullParser): IQ = {
    val applianceRemoved = new ApplianceRemoved
    var remaining = true
    while(remaining) {
      val eventType = parser.next()
      if(eventType == XmlPullParser.START_TAG) {
        val name = parser.getName()
        name match {
          case "name" => applianceRemoved.setName(parser.nextText())
          case "version" => applianceRemoved.setVersion(parser.nextText())
          case _ => Unit
        }
      } else if(eventType == XmlPullParser.END_TAG) {
        if(parser.getName() == ApplianceSetProvider.element)
          remaining = false
      }
    }
    return applianceRemoved
  }
}

object ApplianceRemovedProvider {
  val namespace = "appliance:removed:xamin"
  val element = "appliance"
}

// vim: set ts=4 sw=4 et:
