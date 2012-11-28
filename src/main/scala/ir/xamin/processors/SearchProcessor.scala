package ir.xamin.processors

import ir.xamin.Appliance
import ir.xamin.Processor
import ir.xamin.packet.receive.Search
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.XMPPConnection
import com.redis._
import com.github.seratch.scalikesolr._
import org.jivesoftware.smack.PacketListener
import org.jivesoftware.smack.packet.{IQ, Packet, XMPPError}
import org.jivesoftware.smack.filter.PacketFilter

/** this packet processes search requests
 */
class SearchProcessor(redisClient: RedisClient, xmppConnection: XMPPConnection, solrClient: SolrClient) extends Processor(redisClient, xmppConnection, solrClient) {
  // we already have namespace / tag name as filters of this
  // processor so just checking packet type is enough
  object filter extends PacketFilter {
    def accept(p:Packet):Boolean = {
      return p match {
        case p:Search => true
        case _ => false
      }
    }
  }

  /** smack sends us the packets that passed filtering here
   * @param packet the packet that should be processed
   */
  def processPacket(packet:Packet):Unit = {
    try {
      packet match {
        case search: Search => processSearch(search)
      }
    } catch {
      case _ => {
        packet match {
          case iq:IQ => xmpp.sendPacket(IQ.createErrorResponse(
            iq,
            new XMPPError(
              XMPPError.Condition.interna_server_error
            )
          ))
        }
      }
    }
  }

  /** processes Search packets
   * @param search the packet to be processed
   */
  def processSearch(search:Search):Unit = {
    val packages = redis.keys("Appliance:"+search.getQuery)
    var appliances = MutableList[Appliance]()
    for {
      ps <- packages
      p <- ps
    } {
      val appliance = getAppliance(p.get, 0)
      if (!appliance.isEmpty) {
        appliances += appliance.get
      }
    }
    val result = search.createResultIQ(appliances)
    xmpp.sendPacket(result)
  }
}

// vim: set ts=4 sw=4 et:
