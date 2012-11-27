package ir.xamin.processors

import ir.xamin.Appliance
import ir.xamin.packet.receive.HashSearch
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
class HashSearchProcessor(redisClient: RedisClient, xmppConnection: XMPPConnection, solrClient: SolrClient) extends PacketListener {
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
      case search:HashSearch => processSearch(search)
    }
  }

  /** processes Search packets
   * @param search the packet to be processed
   */
  def processSearch(search:HashSearch):Unit = {
    var appliances = MutableList[Appliance]()
    val hash = search.getHash
    for {
      theHash <- hash
    } {
      val nameversion = redis.hmget("ApplianceHash", theHash)
      if(!nameversion.isEmpty) {
        // the value must be like NAME:VERSION
        val splitTmp = nameversion.get.apply(theHash).split(":")
        val name = splitTmp(0)
        val version = splitTmp(1)
        val appliance = getAppliance(name, version)
        if(!appliance.isEmpty) {
          appliances += appliance.get
        }
      }
    }
    val result = search.createResultIQ(appliances)
    xmpp.sendPacket(result)
  }

  /** fetches Appliance based on name / index
   * @param name the name of appliance
   * @param index the index of appliance in list
   * @return appliance
   */
  def getAppliance(name:String, index:Int):Option[Appliance] = {
    val key = "Appliance:"+name
    val encodedAppliance = redis.lindex(key, index)
    if(!encodedAppliance.isEmpty){
      val appliance = fromjson[Appliance](Js(encodedAppliance.get))
      return Some(appliance)
    }
    return None
  }

  /** fetches index of Appliance based on name / version
   * @param name the name of appliance
   * @param version the version of appliance
   * @return index
   */
  def getApplianceIndex(name:String, version:String):Option[Int] = {
    val key = "Appliance:"+name
    var index = 0
    if(version != null){
      val indexTmp = redis.get("appliance_version_to_index:"+name+":"+version)
      if(!indexTmp.isEmpty){
        // the index contains number of items we need to pass from
        // end of list, we need to convert it to index from start
        val len = redis.llen(key).get
        return Some(len-indexTmp.get.toInt-1)
      }
    } else
      return Some(0)
    return None
  }

  /** fetches Appliance based on name / version
   * @param name the name of appliance
   * @param version the version of appliance
   * @return appliance
   */
  def getAppliance(name:String, version:String):Option[Appliance] = {
    val key = "Appliance:"+name
    val index = getApplianceIndex(name, version)
    if(index.isEmpty)
      return None
    return getAppliance(name, index.get)
  }
}

// vim: set ts=4 sw=4 et:
