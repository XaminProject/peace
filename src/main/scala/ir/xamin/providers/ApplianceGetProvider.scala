package ir.xamin.providers

import ir.xamin.packet.receive.ApplianceGet
import org.jivesoftware.smack.packet.IQ
import org.jivesoftware.smack.provider.IQProvider
import org.xmlpull.v1.XmlPullParser

class ApplianceGetProvider extends IQProvider {
  def parseIQ(parser: XmlPullParser): IQ = {
    val applianceGet = new ApplianceGet
    var remaining = true
    while(remaining) {
      val eventType = parser.next()
      if(eventType == XmlPullParser.START_TAG) {
        val name = parser.getName()
        name match {
          case "name" => applianceGet.setName(parser.nextText())
          case "version" => applianceGet.setVersion(parser.nextText())
          case _ => Unit
        }
      } else if(eventType == XmlPullParser.END_TAG) {
        if(parser.getName() == ApplianceSetProvider.element)
          remaining = false
      }
    }
    return applianceGet
  }
}

object ApplianceGetProvider {
  val namespace = "appliance:get:xamin"
  val element = "appliance"
}

// vim: set ts=4 sw=4 et:
