package ir.xamin.packet.receive

import ir.xamin.packet.reply.{ApplianceInstall => ReplyApplianceInstall}
import ir.xamin.providers.ApplianceInstallProvider
import ir.xamin.Appliance
import scala.xml._
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.packet.{IQ, Packet}

class ApplianceInstall extends IQ {
  private var name:String = _
  private var version:String = _
  private var base:String = _

  setType(IQ.Type.SET)

  def getName = name

  def setName(v:String) = name = v

  def getVersion = version

  def setVersion(v:String) = version = v

  def getBase = base

  def setBase(v:String) = base = v

  def getChildElementXML:String = {
    val ns = ApplianceInstallProvider.namespace
    val tags = MutableList[Elem]()
    tags += <name>{name}</name>
    tags += <version>{version}</version>
    if (base!=null) {
      tags += <base>{base}</base>
    }
    <install xmlns={ns}>
      {tags}
    </install>.toString
  }

  def createResultIQ(appliance: Appliance):ReplyApplianceInstall = {
    val applianceInstall = new ReplyApplianceInstall
    applianceInstall.setPacketID(getPacketID())
    applianceInstall.setFrom(getTo())
    applianceInstall.setTo(getFrom())
    applianceInstall.setAppliance(appliance)
    applianceInstall
   }
}

// vim: set ts=4 sw=4 et:
