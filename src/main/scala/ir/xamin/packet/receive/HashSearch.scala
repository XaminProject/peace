package ir.xamin.packet.receive

import ir.xamin.packet.reply.{HashSearch => ReplyHashSearch}
import ir.xamin.providers.HashSearchProvider
import ir.xamin.Appliance
import scala.xml._
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.packet.{Packet, IQ}

/** a class that represents the search stanza
 */
class HashSearch extends IQ {
  private var hash:List[String] = _

  // set type of iq
  setType(IQ.Type.GET)

  /** getter for hash list
   * @return a list which contains hashes we're looking for
   */
  def getHash = hash

  /** setter for hash list
   * @param a list containing the file name hashes
   */
  def setHash(value:List[String]):Unit = hash = value

  /** creates inner xml of iq
   * @return a string which is inner xml of iq
   */
  def getChildElementXML:String = {
    val ns = HashSearchProvider.namespace
    val appliances = hash.flatMap { s => <appliance hash={s}/> }
    <search xmlns={ns}>{appliances}</search>.toString
  }

  /** creates reply of request
   * @param packages a map of hash-string to appliances that match the search
   * @return the reply
   */
  def createResultIQ(packages:Map[String, Appliance]) = {
    val search = new ReplyHashSearch
    search setPacketID getPacketID
    search setFrom getTo
    search setTo getFrom
    search setPackages packages
    search
  }
}

// vim: set ts=4 sw=4 et:
