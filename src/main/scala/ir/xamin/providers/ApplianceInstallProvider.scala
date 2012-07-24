package ir.xamin.providers

import ir.xamin.packet.ApplianceInstall
import org.jivesoftware.smack.packet.IQ
import org.jivesoftware.smack.provider.IQProvider
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParser._

class ApplianceInstallProvider extends IQProvider {
  def parseIQ(parser: XmlPullParser): IQ = {
    val applianceInstall = new ApplianceInstall
    var remaining = true
    while(remaining) {
      val eventType = parser.next()
      if(eventType == XmlPullParser.START_TAG) {
        val name = parser.getName()
        name match {
          case "name" => applianceInstall.setName(parser.nextText())
          case "version" => applianceInstall.setVersion(parser.nextText())
          case "base" => applianceInstall.setBase(parser.nextText())
          case _ => Unit
        }
      } else if(eventType == XmlPullParser.END_TAG) {
        if(parser.getName() == ApplianceInstallProvider.element)
          remaining = false
      }
    }
    applianceInstall
  }
}

object ApplianceInstallProvider {
  val namespace = "appliance:install:xamin"
  val element = "install"
}

// vim: set ts=4 sw=4 et:
