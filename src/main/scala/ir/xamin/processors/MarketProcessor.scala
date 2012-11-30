package ir.xamin.processors

import ir.xamin.Processor
import ir.xamin.packet.receive.{MarketInstall, MarketRemove}
import org.jivesoftware.smack.XMPPConnection
import com.redis._
import com.github.seratch.scalikesolr._
import org.jivesoftware.smack.PacketListener
import org.jivesoftware.smack.packet.{IQ, Packet}
import org.jivesoftware.smack.filter.PacketFilter
import org.jivesoftware.smack.util.StringUtils

/** this class processes all packets that are prefixed with
 * Market in ir.xamin.packet.receive
 */
class MarketProcessor(redisClient:RedisClient, xmppConnection:XMPPConnection, solrClient:SolrClient, market:Array[String]) extends Processor(redisClient, xmppConnection, solrClient) {
  /** this object filter the packets that we process
   */
  object filter extends PacketFilter {
    def accept(p: Packet):Boolean = {
      // check if we've whitelist of jid for markets to work with
      if(market.length == 1 && market(0) == "" || market.indexOf(StringUtils.parseBareAddress(p.getFrom)) > -1)
      {
        return p match {
          case p:MarketInstall => true
          case p:MarketRemove => true
          case _ => false
        }
      }
      false
    }
  }

  /** smack sends us packets that passed the filter here
   * @param packet the packet that passed the filter
   */
  def processPacket(packet: Packet):Unit = {
    try {
      packet match {
        case i:MarketInstall => processMarketInstall(i)
        case r:MarketRemove => processMarketRemove(r)
      }
    } catch {
      case e:Exception => internalError(e, packet)
    }
  }

  /** processes MarketInstall packets which are made by
   * market based on user request
   * @param i packet to be processed
   */
  def processMarketInstall(i: MarketInstall):Unit = {
    // we just work as a router here, nothing special
    xmpp.sendPacket(i.createResultIQ)
    xmpp.sendPacket(IQ.createResultIQ(i))
  }

  /** processes MarketRemove packets which tells target archipel
   * to remove appliances (this packet has been made by market
   * based on user request)
   * @param r packet to be processed
   */
  def processMarketRemove(r: MarketRemove):Unit = {
    // we just work as a router here, nothing special
    xmpp.sendPacket(r.createResultIQ)
    xmpp.sendPacket(IQ.createResultIQ(r))
  }
}

// vim: set ts=4 sw=4 et:
