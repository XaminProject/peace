package ir.xamin.providers

import ir.xamin.packet.receive.ApplianceSet
import org.jivesoftware.smack.packet.IQ
import org.jivesoftware.smack.provider.IQProvider
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParser._

/** this class parses received xml to create an ApplianceSet object
 */
class ApplianceSetProvider extends IQProvider {
  /** parses the received stanza
   * @param parser the XmlPullParser object of stanza
   * @return the ApplianceSet representation of stanza
   */
  def parseIQ(parser: XmlPullParser): IQ = {
    val applianceSet = new ApplianceSet
    var remaining = true
    var tags = List[String]()
    var images = List[Map[String, String]]()
    var inImage = false
    var image = Map[String, String]()
    while(remaining) {
      val eventType = parser.next()
      if(eventType == XmlPullParser.START_TAG) {
        val name = parser.getName()
        if(name == "image") {
          inImage = true
          image = Map[String, String]()
        }
        name match {
          case "name" => applianceSet.setName(parser.nextText())
          case "version" => applianceSet.setVersion(parser.nextText())
          case "description" =>
            if(inImage)
              image += ("description" -> parser.nextText())
            else
              applianceSet.setDescription(parser.nextText())
          case "url" => applianceSet.setURL(parser.nextText())
          case "author" => applianceSet.setAuthor(parser.nextText())
          case "tag" => tags = parser.nextText() :: tags
          case "cpu" => applianceSet.setCPU(parser.nextText().toInt)
          case "memory" => applianceSet.setMemory(parser.nextText().toInt)
          case "storage" => applianceSet.setStorage(parser.nextText().toInt)
          case "category" => applianceSet.setCategory(parser.nextText())
          case "icon" => applianceSet.setIcon(parser.nextText())
          case "title" => image += ("title" -> parser.nextText())
          case "path" => image += ("path" -> parser.nextText())
          case _ => Unit
        }
      } else if(eventType == XmlPullParser.END_TAG) {
        if(parser.getName() == "image") {
          images = image :: images
          inImage = false
        }
        if(parser.getName() == ApplianceSetProvider.element)
          remaining = false
      }
    }
    applianceSet setTags tags
    applianceSet setImages images
    applianceSet
  }
}

object ApplianceSetProvider {
  val namespace = "appliance:set:xamin"
  val element = "appliance"
}

// vim: set ts=4 sw=4 et:
