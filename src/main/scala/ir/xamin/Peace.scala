package ir.xamin

import com.redis._
import org.jivesoftware.smack._

class Peace(host: Option[String], username: Option[String], password: Option[String], resource: Option[String]) {
    println(username.get+"@"+host.get+"/"+resource.getOrElse("peace"))
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

