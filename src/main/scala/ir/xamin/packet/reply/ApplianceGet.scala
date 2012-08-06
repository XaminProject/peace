package ir.xamin.packet.reply

import ir.xamin.providers.ApplianceGetProvider
import ir.xamin.Appliance
import scala.xml._
import org.jivesoftware.smack.packet.{IQ, Packet}

class ApplianceGet extends IQ {
  private var appliance:Appliance = _

  setType(IQ.Type.RESULT)

  def getAppliance = appliance

  def setAppliance(a: Appliance) = appliance = a

  def getChildElementXML:String = {
    val ns = ApplianceGetProvider.namespace
    appliance match {
      case Appliance(n, v, d, u, a, e, t) => {
        val tags = t.flatMap { s => <tag>{s}</tag> }
        <appliance xmlns={ ns }>
          <name>{ n }</name>
          <version>{ v }</version>
          <description>{ d }</description>
          <author>{ a }</author>
          <tags>{tags}</tags>
        </appliance>.toString
      }
      case _ => ""
    }
  }
}

// vim: set ts=4 sw=4 et:
