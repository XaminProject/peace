package ir.xamin.processors

import ir.xamin.packet.receive.{MarketInstall, MarketRemove}
import org.jivesoftware.smack.XMPPConnection
import com.redis._
import org.jivesoftware.smack.PacketListener
import org.jivesoftware.smack.packet.{IQ, Packet}
import org.jivesoftware.smack.filter.PacketFilter
import org.jivesoftware.smack.util.StringUtils

class MarketProcessor(redisClient:RedisClient, xmppConnection:XMPPConnection, market:Array[String]) extends PacketListener {
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
  val xmpp = xmppConnection
  val redis = redisClient

  def processPacket(packet: Packet):Unit = {
    packet match {
      case i:MarketInstall => processMarketInstall(i)
      case r:MarketRemove => processMarketRemove(r)
    }
  }

  def processMarketInstall(i: MarketInstall):Unit = {
    xmpp.sendPacket(i.createResultIQ)
    xmpp.sendPacket(IQ.createResultIQ(i))
  }

  def processMarketRemove(r: MarketRemove):Unit = {
    xmpp.sendPacket(r.createResultIQ)
    xmpp.sendPacket(IQ.createResultIQ(r))
  }
}

// vim: set ts=4 sw=4 et:
