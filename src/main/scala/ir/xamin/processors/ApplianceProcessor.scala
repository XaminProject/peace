package ir.xamin.processors

import ir.xamin.Appliance
import ir.xamin.packet.{ApplianceItem, OwnerBehalfSubscribe}
import ir.xamin.packet.receive.{ApplianceSet, ApplianceGet, ApplianceInstall, ApplianceEnable}
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.XMPPConnection
import com.redis._
import sjson.json._
import dispatch.json._
import JsonSerialization._
import org.jivesoftware.smack.PacketListener
import org.jivesoftware.smack.packet.{IQ, Packet}
import org.jivesoftware.smack.filter.PacketFilter
import org.jivesoftware.smack.util.StringUtils
import org.jivesoftware.smackx.pubsub._

/** this class processes the packets that are prefixed with
 * Appliance in ir.xamin.packet.receive
 */
class ApplianceProcessor(redisClient: RedisClient, xmppConnection: XMPPConnection, rms: Array[String]) extends PacketListener {
  /** this objects filters the packets that we can process
   */
  object filter extends PacketFilter {
    def accept(p: Packet):Boolean = {
      return p match {
        case p:ApplianceGet => true
        case p:ApplianceSet => {
          // check if jid is in list of valid rms jids
          if((rms.length == 1 && rms(0) == "") || rms.indexOf(StringUtils.parseBareAddress(p.getFrom())) > -1)
            true
          else
            false
        }
        case p:ApplianceEnable => {
          // check if jid is in list of valid rms jids
          if((rms.length == 1 && rms(0) == "") || rms.indexOf(StringUtils.parseBareAddress(p.getFrom())) > -1)
            true
          else
            false
        }
        case p:ApplianceInstall => true
        case _ => false
      }
    }
  }
  val xmpp = xmppConnection
  val redis = redisClient

  /** smack sends us the packets that we can process here
   * @param packet the packet that passed filter
   */
  def processPacket(packet: Packet):Unit = {
    packet match {
      case set: ApplianceSet => processApplianceSet(set)
      case enable: ApplianceEnable => processApplianceEnable(enable)
      case get: ApplianceGet => processApplianceGet(get)
      case install: ApplianceInstall => processApplianceInstall(install)
    }
  }

  /** stores relation between tags and appliance in redis
   * @param appliance name of appliance
   * @param tags list of tags
   */
  def saveTags(appliance: String, tags: List[String]):Unit = {
    for(tag <- tags)
    {
      redis.sadd("tags", tag)
      redis.sadd("tag:"+tag, appliance)
    }
  }

  /** processes the ApplianceSet packet
   * @param set the packet to be processed
   */
  def processApplianceSet(set: ApplianceSet):Unit = {
    val appliance = new Appliance(set.getName, set.getVersion,
      set.getDescription, set.getURL, set.getAuthor, false, set.getTags,
      set.getCPU, set.getMemory, set.getStorage)
    val key = "Appliance:"+set.getName
    saveTags(set.getName, set.getTags)
    val manager = new PubSubManager(xmpp)
    val isNew = !redis.exists(key)
    redis.lpush(key, tojson[Appliance](appliance))
    xmpp.sendPacket(IQ.createResultIQ(set))
    if(isNew) {
      val form = new ConfigureForm(FormType.submit)
      form.setAccessModel(AccessModel.open)
      form.setDeliverPayloads(true)
      form.setNotifyRetract(true)
      form.setPersistentItems(true)
      form.setPublishModel(PublishModel.open)
      val leaf = manager.createNode(set.getName, form)
    } else {
      val node = manager.getNode(set.getName)
      node match {
        case n:LeafNode => n.send(new PayloadItem(new ApplianceItem(set.getVersion)))
      }
    }
  }

  /** processes ApplianceEnable packet
   * @param enable packet to be processed
   */
  def processApplianceEnable(enable: ApplianceEnable):Unit = {
    val name = enable.getName
    val version = enable.getVersion
    val key = "Appliance:"+name
    val len = redis.llen(key)
    if(!len.isEmpty) {
      val allVersions = redis.lrange(key, 0, len.get-1)
      var index = 0
      for (ap <- allVersions.get) {
        val appliance = fromjson[Appliance](Js(ap.get))
        if(appliance.version==version) {
          val enabledAppliance = new Appliance(
            appliance.name,
            appliance.version,
            appliance.description,
            appliance.url,
            appliance.author,
            true,
            appliance.tags,
            appliance.cpu,
            appliance.memory,
            appliance.storage
          )
          redis.lset(key, index, tojson[Appliance](enabledAppliance))
          return xmpp.sendPacket(enable.createResultIQ(enabledAppliance))
        }
        index = index + 1
      }
    }
    xmpp.sendPacket(IQ.createResultIQ(enable))
  }

  /** processes ApplianceGet packet
   * @param get packet to be processed
   */
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

  /** subscribes a JID to pubsub node of an Appliance
   * @param jid a string which is jid that we want to be subscribed to node
   * @param appliance a string which is name of appliance (=node)
   */
  def subscribeJIDToAppliance(jid:String, appliance:String):Unit = {
    xmpp.sendPacket(new OwnerBehalfSubscribe(xmpp, jid, appliance))
  }

  /** stores relation of appliance and jids that have installed
   * the appliance
   * @param jid a string which is jid of archipel that has installed an appliance
   * @param appliance a string which is name of appliance that has been installed
   * @param version a string which is version of appliance that has been installed
   */
  def applianceInstalled(jid:String, appliance:String, version:String):Unit = {
    redis.sadd("appliance_to_installers:"+appliance+":"+version, jid)
    redis.sadd("installer_to_appliances:"+jid, appliance+":"+version)
  }

  /** processes ApplianceInstall packets
   * @param install packet to be processed
   */
  def processApplianceInstall(install: ApplianceInstall):Unit = {
    // lets assume we've 6 5 4 3 2 1 versions for requested package
    // and requested version is 5 and base is 2, we gonna give details
    // of requested version + history (3, 4, 5)
    val name = install.getName
    val version = install.getVersion
    val base = install.getBase
    val key = "Appliance:"+name
    var target:Appliance = null
    var versionHistory = MutableList[Appliance]()
    val len = redis.llen(key)
    if(!len.isEmpty) {
      val allVersions = redis.lrange(key, 0, len.get-1)
      for (ap <- allVersions.get) {
        val appliance = fromjson[Appliance](Js(ap.get))
        if(target == null) {
          if(version == null || appliance.version == version) {
            // no specific version requested, let's work with last one
            target = appliance
          }
        }
        // we want to run it on each iteration of loop, so don't make
        // it an else of above if
        if(target != null) {
          if(base == null) {
            // user has not specified base so send the result
            subscribeJIDToAppliance(install.getFrom, target.name)
            applianceInstalled(install.getFrom, target.name, target.version)
            return xmpp.sendPacket(install.createResultIQ(target))
          } else {
            if(appliance.version==base) {
              // if we've reached the requested base send the result
              subscribeJIDToAppliance(install.getFrom, target.name)
              applianceInstalled(install.getFrom, target.name, target.version)
              val result = install.createResultIQ(target)
              result setBase base
              result.setHistory(versionHistory)
              return xmpp.sendPacket(result)
            }
            // prepend old appliance to versionHistory
            appliance +=: versionHistory
          }
        }
      }
    }
    xmpp.sendPacket(IQ.createResultIQ(install))
  }
}
// vim: set ts=4 sw=4 et:
