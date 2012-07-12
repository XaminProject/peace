package ir.xamin.processors

import ir.xamin.Appliance
import ir.xamin.packet.ApplianceSet
import ir.xamin.providers.ApplianceSetProvider
import org.jivesoftware.smack.XMPPConnection
import com.redis._
import sjson.json._
import JsonSerialization._
import org.jivesoftware.smack.PacketListener
import org.jivesoftware.smack.packet.{IQ, Packet}
import org.jivesoftware.smack.filter.{IQTypeFilter, AndFilter, PacketExtensionFilter}

class ApplianceProcessor(redisClient: RedisClient, xmppConnection: XMPPConnection) extends PacketListener {
  val filter = new IQTypeFilter(IQ.Type.SET)
  val xmpp = xmppConnection
  val redis = redisClient

  def processPacket(packet: Packet):Unit = {
    packet match {
      case set: ApplianceSet => processApplianceSet(set)
    }
  }

  def processApplianceSet(set: ApplianceSet):Unit = {
    val appliance = new Appliance(set.getName, set.getVersion,
      set.getDescription, set.getURL, set.getAuthor)
    redis.lpush("Appliance:"+set.getName, tojson[Appliance](appliance))
    xmpp.sendPacket(IQ.createResultIQ(set))
  }
}
// vim: set ts=4 sw=4 et:
