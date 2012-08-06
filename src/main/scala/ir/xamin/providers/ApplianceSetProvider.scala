package ir.xamin.providers

import ir.xamin.packet.receive.ApplianceSet
import org.jivesoftware.smack.packet.IQ
import org.jivesoftware.smack.provider.IQProvider
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParser._

class ApplianceSetProvider extends IQProvider {
  def parseIQ(parser: XmlPullParser): IQ = {
    val applianceSet = new ApplianceSet
    var remaining = true
    var tags = List[String]()
    while(remaining) {
      val eventType = parser.next()
      if(eventType == XmlPullParser.START_TAG) {
        val name = parser.getName()
        name match {
          case "name" => applianceSet.setName(parser.nextText())
          case "version" => applianceSet.setVersion(parser.nextText())
          case "description" => applianceSet.setDescription(parser.nextText())
          case "url" => applianceSet.setURL(parser.nextText())
          case "author" => applianceSet.setAuthor(parser.nextText())
          case "tag" => tags = parser.nextText() :: tags
          case _ => Unit
        }
      } else if(eventType == XmlPullParser.END_TAG) {
        if(parser.getName() == ApplianceSetProvider.element)
          remaining = false
      }
    }
    applianceSet setTags tags
    applianceSet
  }
}

object ApplianceSetProvider {
  val namespace = "appliance:set:xamin"
  val element = "appliance"
}

// vim: set ts=4 sw=4 et:
