package ir.xamin.processors

import ir.xamin.Appliance
import ir.xamin.packet.receive.Search
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.XMPPConnection
import sjson.json._
import dispatch.json._
import JsonSerialization._
import com.redis._
import com.github.seratch.scalikesolr._
import org.jivesoftware.smack.PacketListener
import org.jivesoftware.smack.packet.{IQ, Packet}
import org.jivesoftware.smack.filter.{IQTypeFilter, AndFilter, PacketExtensionFilter}

/** this packet processes search requests
 */
class SearchProcessor(redisClient: RedisClient, xmppConnection: XMPPConnection, solrClient: SolrClient) extends PacketListener {
  // we already have namespace / tag name as filters of this
  // processor so just checking packet type is enough
  val filter = new IQTypeFilter(IQ.Type.GET)
  val xmpp = xmppConnection
  val redis = redisClient
  val solr = solrClient

  /** smack sends us the packets that passed filtering here
   * @param packet the packet that should be processed
   */
  def processPacket(packet: Packet):Unit = {
    packet match {
      case search: Search => processSearch(search)
    }
  }

  /** processes Search packets
   * @param search the packet to be processed
   */
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
