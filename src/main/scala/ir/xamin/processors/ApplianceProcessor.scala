package ir.xamin.processors

import ir.xamin.Appliance
import ir.xamin.Processor
import ir.xamin.packet.{ApplianceItem, OwnerBehalfSubscribe}
import ir.xamin.packet.receive.{ApplianceSet, ApplianceGet, ApplianceInstall, ApplianceEnable, ApplianceRemoved}
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.XMPPConnection
import com.redis._
import com.github.seratch.scalikesolr._
import sjson.json._
import dispatch.json._
import JsonSerialization._
import org.jivesoftware.smack.packet.{IQ, Packet, XMPPError}
import org.jivesoftware.smack.filter.PacketFilter
import org.jivesoftware.smack.util.StringUtils
import org.jivesoftware.smackx.pubsub._

/** this class processes the packets that are prefixed with
 * Appliance in ir.xamin.packet.receive
 */
class ApplianceProcessor(redisClient: RedisClient, xmppConnection: XMPPConnection, solrClient: SolrClient, rms: Array[String]) extends Processor(redisClient, xmppConnection, solrClient) {
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
        case p:ApplianceRemoved => true
        case _ => false
      }
    }
  }
  val platform = compat.Platform

  /** smack sends us the packets that we can process here
   * @param packet the packet that passed filter
   */
  def processPacket(packet:Packet):Unit = {
    try {
      packet match {
        case set: ApplianceSet => processApplianceSet(set)
        case enable: ApplianceEnable => processApplianceEnable(enable)
        case get: ApplianceGet => processApplianceGet(get)
        case install: ApplianceInstall => processApplianceInstall(install)
        case removed: ApplianceRemoved => processApplianceRemoved(removed)
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

  /** stores relation between tags and appliance in redis
   * @param appliance name of appliance
   * @param tags list of tags
   */
  def saveTags(appliance:String, tags:List[String]):Unit = {
    val rating = getApplianceRating(appliance)
    for(tag <- tags)
    {
      redis.zincrby("tags", 1, tag)
      redis.zadd("tag:"+tag, rating, appliance)
    }
  }

  /** stores relation between category and appliance in redis
   * @param appliance name of appliance
   * @param tags list of tags
   */
  def saveCategory(appliance:String, category:String):Unit = {
    val rating = getApplianceRating(appliance)
    redis.zincrby("categories", 1, category)
    redis.zadd("category:"+category, rating, appliance)
  }

  /** removes relation between tags and appliances in redis
   * @param name the name of appliance
   * @param tags list of tags
   */
  def removeTags(name:String, tags:List[String]):Unit = {
    for(tag <- tags)
    {
      redis.zincrby("tags", -1, tag)
      redis.zrem("tag:"+tag, name)
    }
  }

  /** stores relation between appliance and author in redis
   * @param appliance name of appliance
   * @param author
   */
  def saveAuthor(appliance:String, author:String):Unit = {
    val key = "author_to_appliance:"+author;
    redis.sadd(key, appliance)
    val appliances = redis.smembers(key).get
    var total = 0
    for (name <- appliances) {
      total += getApplianceRating(name.get).toInt
    }
    redis.zadd("Authors", total, author)
  }

  /**
   * stores appliance documents into solr
   * @param appliance the json presentation of appliance that should be updated
   */
  def updateSolr(appliance:Appliance):Unit = {
    var json = tojson[Appliance](appliance).toString
    val id = "\"id\":\""+appliance.name+":"+appliance.version+"\","
    json = "{"+id+json.substring(1)
    val request = new UpdateRequest()
    val document = SolrDocument(
      writerType=WriterType.JSON,
      rawBody=json )
    request.documents = List(document)
    try {
      val response = solr.doUpdateDocuments(request)
      solr.doCommit(new UpdateRequest)
    } catch {
      case e:Exception => println(e)
    }
  }

  /** processes the ApplianceSet packet
   * @param set the packet to be processed
   */
  def processApplianceSet(set: ApplianceSet):Unit = {
    val appliance = new Appliance(set.getName, set.getVersion,
      set.getDescription, set.getURL, set.getAuthor, false, set.getTags,
      set.getCPU, set.getMemory, set.getStorage, set.getCategory,
      set.getImages, set.getIcon, platform.currentTime, set.getHome,
      set.getPayment)
    val hash = ("([0-9A-Za-z]+).xvm2$".r findFirstMatchIn appliance.url) map (_.group(1))
    val key = "Appliance:"+appliance.name
    // save relation of appliance <-> author
    saveAuthor(set.getName, set.getAuthor)
    val manager = new PubSubManager(xmpp)
    val isNew = !redis.exists(key)
    // index of specific version from end of list
    val versionRightIndex = redis.llen(key)
    redis.lpush(key, tojson[Appliance](appliance))
    redis.set("appliance_version_to_index:"+appliance.name+":"+appliance.version, versionRightIndex.get)
    if(!hash.isEmpty) {
      redis.hmset("ApplianceHash", Map(hash.get -> (appliance.name+":"+appliance.version)))
    }
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
    val index = getApplianceIndex(name, version)
    if(!index.isEmpty) {
      val ap = getAppliance(name, index.get)
      if(!ap.isEmpty) {
        val appliance = ap.get
        val previousVersion = getAppliance(name, index.get+1)
        if (!previousVersion.isEmpty) {
          // remove tags of prevous version
          removeTags(name, previousVersion.get.tags)
        }
        // save relation of appliance <-> tags
        saveTags(name, appliance.tags)
        // save relation of appliance <-> category
        saveCategory(name, appliance.category)
        // save appliances in a sorted set with score of creation date
        redis.zadd("Appliances", appliance.creation, name)
        // ok, let's change the enable property and store the appliance
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
          appliance.storage,
          appliance.category,
          appliance.images,
          appliance.icon,
          appliance.creation,
          appliance.home,
          appliance.payment
        )
        redis.lset(key, index.get, tojson[Appliance](enabledAppliance))
        // save the appliance into full text search engine
        updateSolr(enabledAppliance)
        return xmpp.sendPacket(enable.createResultIQ(enabledAppliance))
      }
    }
    xmpp.sendPacket(IQ.createResultIQ(enable))
  }

  /** processes ApplianceRemoved packet
   * @param removed packet to be processed
   */
  def processApplianceRemoved(removed: ApplianceRemoved):Unit = {
    val name = removed.getName
    val version = removed.getVersion
    val appliance = getAppliance(name, version)
    if(!appliance.isEmpty){
      applianceRemoved(removed.getFrom, name, version)
      return xmpp.sendPacket(removed.createResultIQ(appliance.get))
    }
    xmpp.sendPacket(IQ.createResultIQ(removed))
  }

  /** processes ApplianceGet packet
   * @param get packet to be processed
   */
  def processApplianceGet(get: ApplianceGet):Unit = {
    val name = get.getName
    val version = get.getVersion
    val appliance = getAppliance(name, version)
    if(appliance.isEmpty){
      xmpp.sendPacket(IQ.createResultIQ(get))
    } else {
      xmpp.sendPacket(get.createResultIQ(appliance.get))
    }
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

  /** removes relation between appliance and jid that previously
   * has installed the appliance
   * @param jid a string which is jid of archipel that removed the appliance
   * @param appliance the name of removed appliance
   * @param version the version of removed appliance
   */
  def applianceRemoved(jid:String, appliance:String, version:String):Unit = {
    redis.srem("appliance_to_installers:"+appliance+":"+version, jid)
    redis.srem("installer_to_appliances:"+jid, appliance+":"+version)
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
      val allVersions = redis.lrange(key, 0, len.get.toInt-1)
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
