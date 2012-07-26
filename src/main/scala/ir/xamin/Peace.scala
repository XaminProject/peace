package ir.xamin

import providers._
import processors._
import com.redis._
import org.jivesoftware.smack.XMPPConnection
import org.jivesoftware.smack.provider.ProviderManager

class Peace(host: Option[String],
  username: Option[String],
  password: Option[String],
  resource: Option[String],
  redishost: Option[String],
  redisport: Option[Int],
  rmsJid: Option[String]) {
  val redis = new RedisClient(redishost.getOrElse("localhost"), redisport.getOrElse(6379))
  val xmpp = new XMPPConnection(host.get)
  xmpp.connect()
  xmpp.login(username.get, password.get, resource.getOrElse("peace"))
  val providerManager = ProviderManager.getInstance()

  val rms = rmsJid.getOrElse("").split(",")

  registerIQProviders()
  registerProcessors()

  def registerIQProviders() {
    // search
    providerManager.addIQProvider(SearchProvider.element, SearchProvider.namespace, new SearchProvider)
    // appliance (set)
    providerManager.addIQProvider(ApplianceSetProvider.element, ApplianceSetProvider.namespace, new ApplianceSetProvider)
    // appliance (get)
    providerManager.addIQProvider(ApplianceGetProvider.element, ApplianceGetProvider.namespace, new ApplianceGetProvider)
    // appliance (install)
    providerManager.addIQProvider(ApplianceInstallProvider.element, ApplianceInstallProvider.namespace, new ApplianceInstallProvider)
  }

  def registerProcessors() {
    // search
    val searchProcessor = new SearchProcessor(redis, xmpp)
    xmpp.createPacketCollector(searchProcessor.filter)
    xmpp.addPacketListener(searchProcessor, searchProcessor.filter)
    // Appliance (set/get/install)
    val applianceProcessor = new ApplianceProcessor(redis, xmpp, rms)
    xmpp.createPacketCollector(applianceProcessor.filter)
    xmpp.addPacketListener(applianceProcessor, applianceProcessor.filter)
  }
}

object Peace {
  import org.clapper.argot._
  import org.clapper.argot.ArgotConverters._

  val parser = new ArgotParser("Peace", preUsage=Some("Version 0.0.1"))

  private val host = parser.parameter[String]("hostname", "xmpp server", false)
  private val username = parser.parameter[String]("username", "username pf jid", false)
  private val password = parser.parameter[String]("password", "password to be used for authentication", false)
  private val resource = parser.parameter[String]("resource", "resource of jid", true)
  private val redishost = parser.option[String](List("r", "redishost"), "localhost", "redis host")
  private val redisport = parser.option[Int](List("p", "redisport"), "6379", "redis port")
  private val rms = parser.option[String](List("s", "rms"), "jid", "jid of rms instances separated by comma")

  def main(args: Array[String]) {
    try {
      parser.parse(args)
      new Peace(host.value,
          username.value,
          password.value,
          resource.value,
          redishost.value,
          redisport.value,
          rms.value)
      System.in.read()
    } catch {
      case e: ArgotUsageException => println(e.message)
    }
  }
}

