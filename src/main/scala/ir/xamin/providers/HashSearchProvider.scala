package ir.xamin.providers

import ir.xamin.packet.receive.HashSearch
import org.jivesoftware.smack.packet.IQ
import org.jivesoftware.smack.provider.IQProvider
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParser._

/** this class parses received xml chunk as HashSearch packet
 */

class HashSearchProvider extends IQProvider {
  /** parse the received stanza
   * @param parser the XmlPullParser of packet
   * @return the HashSearch presentation of packet
   */
  def parseIQ(parser: XmlPullParser): IQ = {
    val hashSearch = new HashSearch
    var hash = List[String]()
    var remaining = true
    while(remaining) {
      val eventType = parser.next()
      if(eventType == XmlPullParser.START_TAG) {
        val name = parser.getName()
        name match {
          case "appliance" => hash = parser.getAttributeValue("", "hash") :: hash
          case _ => Unit
        }
      } else if(eventType == XmlPullParser.END_TAG) {
        if(parser.getName() == HashSearchProvider.element)
          remaining = false
      }
    }
    hashSearch setHash hash
    return hashSearch
  }
}

object HashSearchProvider
{
  val namespace = "client:hashsearch:xamin"
  val element = "search"
}

// vim: set ts=4 sw=4 et:
