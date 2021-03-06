package ir.xamin

import providers._
import processors._
import com.redis._
import com.github.seratch.scalikesolr._
import org.jivesoftware.smack.XMPPConnection
import org.jivesoftware.smack.provider.ProviderManager
import java.net.URL

/** initializer of peace
 * @param host the xmpp host to connect to
 * @param username the xmpp username
 * @param password the xmpp password
 * @param resource the resource of xmpp to use
 * @param redishost host of redis to connect to
 * @param redisport port of redis
 * @param solrRestAPI address of solr's REST-API
 * @param rmsJid jids that can perform rms specific actions
 * @param marketJid jids that can perform market specific actions
 */
class Peace(host: Option[String],
  username: Option[String],
  password: Option[String],
  resource: Option[String],
  redishost: Option[String],
  redisport: Option[Int],
  solrRestAPI: Option[String],
  rmsJid: Option[String],
  marketJid: Option[String]) {
  // connect to redis
  val redis = new RedisClient(redishost.getOrElse("localhost"), redisport.getOrElse(6379))
  // connect to solr
  val solr = Solr.httpServer(new URL(solrRestAPI.getOrElse("http://localhost:8983/solr"))).newClient
  // connect to xmpp server
  val xmpp = new XMPPConnection(host.get)
  xmpp.connect()
  // authenticate with xmpp server
  xmpp.login(username.get, password.get, resource.getOrElse("peace"))
  val providerManager = ProviderManager.getInstance()

  // split rms / market with comma
  val rms = rmsJid.getOrElse("").split(",")
  val market = marketJid.getOrElse("").split(",")

  // register providers to ProviderManager
  registerIQProviders()
  // register processors to ProviderManager
  registerProcessors()

  /** registers IQ providers
   */
  def registerIQProviders() {
    // search
    providerManager.addIQProvider(SearchProvider.element, SearchProvider.namespace, new SearchProvider)
    // hashsearch
    providerManager.addIQProvider(HashSearchProvider.element, HashSearchProvider.namespace, new HashSearchProvider)
    // appliance (set)
    providerManager.addIQProvider(ApplianceSetProvider.element, ApplianceSetProvider.namespace, new ApplianceSetProvider)
    // appliance (get)
    providerManager.addIQProvider(ApplianceGetProvider.element, ApplianceGetProvider.namespace, new ApplianceGetProvider)
    // appliance (install)
    providerManager.addIQProvider(ApplianceInstallProvider.element, ApplianceInstallProvider.namespace, new ApplianceInstallProvider)
    // appliance (enable)
    providerManager.addIQProvider(ApplianceEnableProvider.element, ApplianceEnableProvider.namespace, new ApplianceEnableProvider)
    // appliance (remove)
    providerManager.addIQProvider(ApplianceRemovedProvider.element, ApplianceRemovedProvider.namespace, new ApplianceRemovedProvider)
    // market (install)
    providerManager.addIQProvider(MarketInstallProvider.element, MarketInstallProvider.namespace, new MarketInstallProvider)
    // market (remove)
    providerManager.addIQProvider(MarketRemoveProvider.element, MarketRemoveProvider.namespace, new MarketRemoveProvider)
  }

  /** registers IQ processors
   */
  def registerProcessors() {
    // search
    val searchProcessor = new SearchProcessor(redis, xmpp, solr)
    xmpp.createPacketCollector(searchProcessor.filter)
    xmpp.addPacketListener(searchProcessor, searchProcessor.filter)
    // hashsearch
    val hashSearchProcessor = new HashSearchProcessor(redis, xmpp, solr)
    xmpp.createPacketCollector(hashSearchProcessor.filter)
    xmpp.addPacketListener(hashSearchProcessor, hashSearchProcessor.filter)
    // Appliance (set/get/install/enable/removed)
    val applianceProcessor = new ApplianceProcessor(redis, xmpp, solr, rms)
    xmpp.createPacketCollector(applianceProcessor.filter)
    xmpp.addPacketListener(applianceProcessor, applianceProcessor.filter)
    // market (install/remove)
    val marketProcessor = new MarketProcessor(redis, xmpp, solr, market)
    xmpp.createPacketCollector(marketProcessor.filter)
    xmpp.addPacketListener(marketProcessor, marketProcessor.filter)
  }
}

object Peace {
  // import argot here, as we'll use it just here
  import org.clapper.argot._
  import org.clapper.argot.ArgotConverters._

  // init argot
  val parser = new ArgotParser("Peace", preUsage=Some("Version 0.2.0"))

  // define options/arguments
  private val host = parser.parameter[String]("hostname", "xmpp server", false)
  private val username = parser.parameter[String]("username", "username pf jid", false)
  private val password = parser.parameter[String]("password", "password to be used for authentication", false)
  private val resource = parser.parameter[String]("resource", "resource of jid", true)
  private val redishost = parser.option[String](List("r", "redishost"), "localhost", "redis host")
  private val redisport = parser.option[Int](List("p", "redisport"), "6379", "redis port")
  private val solr = parser.option[String](List("l", "solr"), "http://localhost:8983/solr", "solr's REST-API address")
  private val rms = parser.option[String](List("s", "rms"), "jid", "jid of rms instances separated by comma")
  private val market = parser.option[String](List("m", "market"), "jid", "jid of market instances separated by comma")

  /** the main entry point of peace
   */
  def main(args: Array[String]) {
    try {
      parser.parse(args)
      new Peace(host.value,
          username.value,
          password.value,
          resource.value,
          redishost.value,
          redisport.value,
          solr.value,
          rms.value,
          market.value)
      // wait for user input then exist the execution
      System.in.read()
    } catch {
      // some arguments seems to be missing, show the help
      case e: ArgotUsageException => Console.err.println(e.message)
    }
  }
}

