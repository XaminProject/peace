package ir.xamin.packet.receive

import ir.xamin.packet.reply.{ApplianceGet => ReplyApplianceGet}
import ir.xamin.providers.ApplianceGetProvider
import ir.xamin.Appliance
import scala.xml._
import org.jivesoftware.smack.packet.{IQ, Packet}

class ApplianceGet extends IQ {
  private var name:String = _
  private var version:String = _

  setType(IQ.Type.GET)

  def getName = name

  def setName(value: String):Unit = name = value

  def getVersion = version

  def setVersion(value: String):Unit = version = value

  def getChildElementXML:String = {
    val ns = ApplianceGetProvider.namespace
    <appliance xmlns={ ns }>
      <name>{name}</name>
      <version>{version}</version>
    </appliance>.toString
  }

  def createResultIQ(appliance: Appliance):ReplyApplianceGet = {
    val applianceGet = new ReplyApplianceGet
    applianceGet.setPacketID(getPacketID())
    applianceGet.setFrom(getTo())
    applianceGet.setTo(getFrom())
    applianceGet.setAppliance(appliance)
    applianceGet
   }
}

// vim: set ts=4 sw=4 et:
