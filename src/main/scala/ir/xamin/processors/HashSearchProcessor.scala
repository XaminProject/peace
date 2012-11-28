package ir.xamin.processors

import ir.xamin.Appliance
import ir.xamin.Processor
import ir.xamin.packet.receive.HashSearch
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.XMPPConnection
import com.redis._
import com.github.seratch.scalikesolr._
import org.jivesoftware.smack.packet.{IQ, Packet, XMPPError}
import org.jivesoftware.smack.filter.PacketFilter

/** this packet processes search requests
 */
class HashSearchProcessor(redisClient: RedisClient, xmppConnection: XMPPConnection, solrClient: SolrClient) extends Processor(redisClient, xmppConnection, solrClient) {
  // we already have namespace / tag name as filters of this
  // processor so just checking packet type is enough
  object filter extends PacketFilter {
    def accept(p: Packet):Boolean = {
      return p match {
        case p:HashSearch => true
        case _ => false
      }
    }
  }

  /** smack sends us the packets that passed filtering here
   * @param packet the packet that should be processed
   */
  def processPacket(packet: Packet):Unit = {
    try {
      packet match {
        case search:HashSearch => processSearch(search)
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
  def processSearch(search:HashSearch):Unit = {
    var appliances = Map[String,Appliance]()
    val hash = search.getHash
    for {
      theHash <- hash
    } {
      val nameversionMap = redis.hmget("ApplianceHash", theHash)
      val nameversion = nameversionMap.get.get(theHash)
      if(!nameversion.isEmpty) {
        // the value must be like NAME:VERSION
        val splitTmp = nameversion.get.split(":")
        val name = splitTmp(0)
        val version = splitTmp(1)
        val appliance = getAppliance(name, version)
        if(!appliance.isEmpty) {
          appliances = appliances + (theHash -> appliance.get)
        }
      }
    }
    val result = search.createResultIQ(appliances)
    xmpp.sendPacket(result)
  }
}

// vim: set ts=4 sw=4 et:
