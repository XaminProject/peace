package ir.xamin

import providers._
import processors._

import com.redis._
import org.jivesoftware.smack.XMPPConnection
import org.jivesoftware.smack.provider.ProviderManager

class Peace(host: Option[String], username: Option[String], password: Option[String], resource: Option[String]) {
  val redis = new RedisClient("localhost", 6379)
  val xmpp = new XMPPConnection(host.get)
  xmpp.connect()
  xmpp.login(username.get, password.get, resource.getOrElse("peace"))
  val providerManager = ProviderManager.getInstance()

  registerIQProviders()
  registerProcessors()

  def registerIQProviders() {
    // search
    providerManager.addIQProvider(SearchProvider.element, SearchProvider.namespace, new SearchProvider)
  }

  def registerProcessors() {
    // search
    val searchProcessor = new SearchProcessor(redis, xmpp)
    xmpp.createPacketCollector(searchProcessor.filter)
    xmpp.addPacketListener(searchProcessor, searchProcessor.filter)
  }
}

object Peace {
  import org.clapper.argot._
  import org.clapper.argot.ArgotConverters._

  val parser = new ArgotParser("Peace", preUsage=Some("Version 0.0.1"))

  val host = parser.parameter[String]("hostname", "xmpp server", false)
  val username = parser.parameter[String]("username", "username pf jid", false)
  val password = parser.parameter[String]("password", "password to be used for authentication", false)
  val resource = parser.parameter[String]("resource", "resource of jid", true)

  def main(args: Array[String])
  {
    try
    {
      parser.parse(args)
      new Peace(host.value, username.value, password.value, resource.value)
    }
    catch
    {
      case e: ArgotUsageException => println(e.message)
    }
  }
}

