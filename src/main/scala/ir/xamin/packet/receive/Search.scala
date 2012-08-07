package ir.xamin.packet.receive

import ir.xamin.packet.reply.{Search => ReplySearch}
import ir.xamin.providers.SearchProvider
import ir.xamin.Appliance
import scala.xml._
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.packet.{Packet, IQ}

/** a class that represents the search stanza
 */
class Search extends IQ {
  private var query:String = _

  // set type of iq
  setType(IQ.Type.GET)

  /** getter for query of search
   * @return a string which is query of search
   */
  def getQuery = query

  /** setter for query of search
   * @return a string which is query of search
   */
  def setQuery(value: String):Unit = query = value

  /** creates inner xml of iq
   * @return a string which is inner xml of iq
   */
  def getChildElementXML:String = {
    val ns = SearchProvider.namespace
    <search xmlns={ns} query={query}/>.toString
  }

  /** creates reply of request
   * @param packages a list of appliances that match the search
   * @return the reply
   */
  def createResultIQ(packages: MutableList[Appliance]):ReplySearch = {
    val search = new ReplySearch
    search setPacketID getPacketID
    search setFrom getTo
    search setTo getFrom
    search setPackages packages
    search
  }
}

// vim: set ts=4 sw=4 et:
