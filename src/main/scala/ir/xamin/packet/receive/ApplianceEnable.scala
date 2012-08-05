package ir.xamin.packet.receive

import ir.xamin.packet.reply.{ApplianceEnable => ReplyApplianceEnable}
import ir.xamin.providers.ApplianceEnableProvider
import ir.xamin.Appliance
import scala.xml._
import org.jivesoftware.smack.packet.{IQ, Packet}

class ApplianceEnable extends IQ {
  private var name:String = _
  private var version:String = _

  setType(IQ.Type.GET)

  def getName = name

  def setName(value: String):Unit = name = value

  def getVersion = version

  def setVersion(value: String):Unit = version = value

  def getChildElementXML:String = {
    val ns = ApplianceEnableProvider.namespace
    <appliance xmlns={ ns }>
      <name>{name}</name>
      <version>{version}</version>
    </appliance>.toString
  }

  def createResultIQ(appliance: Appliance):ReplyApplianceEnable = {
    val applianceEnable = new ReplyApplianceEnable
    applianceEnable setPacketID getPacketID
    applianceEnable setFrom getTo
    applianceEnable setTo getFrom
    applianceEnable
  }
}

// vim: set ts=4 sw=4 et:
