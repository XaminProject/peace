package ir.xamin.processors

import ir.xamin.Appliance
import ir.xamin.packet.Search
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.XMPPConnection
import sjson.json._
import dispatch.json._
import JsonSerialization._
import com.redis._
import org.jivesoftware.smack.PacketListener
import org.jivesoftware.smack.packet.{IQ, Packet}
import org.jivesoftware.smack.filter.{IQTypeFilter, AndFilter, PacketExtensionFilter}

class SearchProcessor(redisClient: RedisClient, xmppConnection: XMPPConnection) extends PacketListener {
  val filter = new IQTypeFilter(IQ.Type.GET)
  val xmpp = xmppConnection
  val redis = redisClient

  def processPacket(packet: Packet):Unit = {
    packet match {
      case search: Search => processSearch(search)
    }
  }

  def processSearch(search: Search):Unit = {
    val packages = redis.keys("Appliance:"+search.getQuery)
    var appliances = MutableList[Appliance]()
    for {
      ps <- packages
      p <- ps
    } {
      appliances += fromjson[Appliance](Js(redis.lindex(p.get, 0).get))
    }
    val result = search.createResultIQ(appliances)
    xmpp.sendPacket(result)
  }
}

// vim: set ts=4 sw=4 et:
