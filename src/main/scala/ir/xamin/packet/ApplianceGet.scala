package ir.xamin.packet

import ir.xamin.providers.ApplianceGetProvider
import ir.xamin.Appliance
import scala.xml._
import org.jivesoftware.smack.packet.{IQ, Packet}

class ApplianceGet extends IQ {
  private var name:String = _
  private var version:String = _
  private var appliance:Appliance = _

  setType(IQ.Type.GET)

  def getName = name

  def setName(value: String):Unit = name = value

  def getVersion = version

  def setVersion(value: String):Unit = version = value

  def getAppliance = appliance

  def setAppliance(a: Appliance) = appliance = a

  def getChildElementXML:String = {
    val ns = ApplianceGetProvider.namespace
    appliance match {
      case Appliance(n, v, d, u, a) => <appliance xmlns={ ns }>
          <name>{ n }</name>
          <version>{ v }</version>
          <description>{ d }</description>
          <url>{ u }</url>
          <author>{ a }</author>
        </appliance>.toString
      case _ => ""
    }
  }

  def createResultIQ(appliance: Appliance):Packet = {
    val applianceGet = new ApplianceGet
    applianceGet.setType(IQ.Type.RESULT)
    applianceGet.setPacketID(getPacketID())
    applianceGet.setFrom(getTo())
    applianceGet.setTo(getFrom())
    applianceGet.setAppliance(appliance)
    applianceGet
   }
}

// vim: set ts=4 sw=4 et:
