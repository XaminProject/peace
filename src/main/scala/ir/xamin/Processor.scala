package ir.xamin

import com.redis._
import org.jivesoftware.smack.XMPPConnection
import com.github.seratch.scalikesolr._
import org.jivesoftware.smack.PacketListener
import org.jivesoftware.smack.packet.{IQ, Packet, XMPPError}
import sjson.json._
import dispatch.json._
import JsonSerialization._

/** base processor class which provides generic methods to work
 * with appliances / redis
 */
abstract class Processor(redisClient: RedisClient, xmppConnection: XMPPConnection, solrClient: SolrClient) extends PacketListener {
  val xmpp = xmppConnection
  val redis = redisClient
  val solr = solrClient

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
        val len = redis.llen(key).get.toInt
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

  /** returns current rating of appliance
   * @param name the name of appliance
   * @return rating
   */
  def getApplianceRating(name:String):Int = {
    val rating = redis.zscore("ratings", name)
    if(rating.isEmpty)
      0
    else
      rating.get.toInt
  }

  /** sends internal error in response of packet
   * @param Exception the thrown exception
   * @param packet the packet that caused problem
   */
  def internalError(e:Exception, packet:Packet):Unit = {
    packet match {
      case iq:IQ => xmpp.sendPacket(IQ.createErrorResponse(
        iq,
        new XMPPError(
          XMPPError.Condition.interna_server_error,
          e.getMessage
        )
      ))
      Console.err.println(e.printStackTrace)
    }
  }
}
