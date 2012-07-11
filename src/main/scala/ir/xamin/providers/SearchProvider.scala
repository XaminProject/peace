
package ir.xamin.providers

import ir.xamin.packet.Search
import org.jivesoftware.smack.packet.IQ
import org.jivesoftware.smack.provider.IQProvider
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParser._

class SearchProvider extends IQProvider {
  def parseIQ(parser: XmlPullParser): IQ = {
    val search = new Search
    search.setQuery(parser.getAttributeValue("", "query"))
    parser.next() // get parser point to end of <search>
    search
  }
}

object SearchProvider
{
  val namespace = "client:search:xamin"
  val element = "search"
}

// vim: set ts=4 sw=4 et:
