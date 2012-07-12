package ir.xamin.processors

import ir.xamin.Appliance
import ir.xamin.packet.{ApplianceSet, ApplianceGet}
import ir.xamin.providers.ApplianceSetProvider
import org.jivesoftware.smack.XMPPConnection
import com.redis._
import sjson.json._
import dispatch.json._
import JsonSerialization._
import org.jivesoftware.smack.PacketListener
import org.jivesoftware.smack.packet.{IQ, Packet}
import org.jivesoftware.smack.filter.PacketFilter

class ApplianceProcessor(redisClient: RedisClient, xmppConnection: XMPPConnection) extends PacketListener {
  object filter extends PacketFilter {
    def accept(p: Packet):Boolean = {
      return p match {
        case p:ApplianceGet => true
        case p:ApplianceSet => true
        case _ => false
      }
    }
  }
  val xmpp = xmppConnection
  val redis = redisClient

  def processPacket(packet: Packet):Unit = {
    packet match {
      case set: ApplianceSet => processApplianceSet(set)
      case get: ApplianceGet => processApplianceGet(get)
    }
  }

  def processApplianceSet(set: ApplianceSet):Unit = {
    val appliance = new Appliance(set.getName, set.getVersion,
      set.getDescription, set.getURL, set.getAuthor)
    redis.lpush("Appliance:"+set.getName, tojson[Appliance](appliance))
    xmpp.sendPacket(IQ.createResultIQ(set))
  }

  def processApplianceGet(get: ApplianceGet):Unit = {
    val name = get.getName
    val version = get.getVersion
    val key = "Appliance:"+name
    if(version == null) {
      val lastVersion = redis.lindex(key, 0)
      if(!lastVersion.isEmpty){
        val appliance = fromjson[Appliance](Js(lastVersion.get))
        return xmpp.sendPacket(get.createResultIQ(appliance))
      }
    } else {
      val len = redis.llen(key)
      if(!len.isEmpty) {
        val allVersions = redis.lrange(key, 0, len.get-1)
        for (ap <- allVersions.get) {
          val appliance = fromjson[Appliance](Js(ap.get))
          if(appliance.version==version) {
            return xmpp.sendPacket(get.createResultIQ(appliance))
          }
        }
      }
    }
    xmpp.sendPacket(IQ.createResultIQ(get))
  }
}
// vim: set ts=4 sw=4 et:
