package ir.xamin.packet

import ir.xamin.providers.ApplianceInstallProvider
import ir.xamin.Appliance
import scala.xml._
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.packet.{IQ, Packet}

class ApplianceInstall extends IQ {
  private var name:String = _
  private var version:String = _
  private var base:String = _
  private var appliance:Appliance = _
  private var history = MutableList[String]()

  setType(IQ.Type.SET)

  def getName = name

  def setName(v:String) = name = v

  def getVersion = version

  def setVersion(v:String) = version = v

  def getBase = base

  def setBase(v:String) = base = v

  def getAppliance = appliance

  def setAppliance(a: Appliance) = appliance = a

  def getHistory = history

  def setHistory(v:MutableList[String]) = history = v

  def getChildElementXML:String = {
    val ns = ApplianceInstallProvider.namespace
    var historyElement:Elem = null
    val versionHistory = MutableList[Elem]()
    for(v <- history)
      versionHistory += <version>{ v }</version>
    if(!versionHistory.isEmpty)
      historyElement = <history>{ versionHistory }</history>
    appliance match {
      case Appliance(n, v, d, u, a) => <appliance xmlns={ ns }>
          <name>{ n }</name>
          <version>{ v }</version>
          <description>{ d }</description>
          <author>{ a }</author>
          <url>{ u }</url>
          { historyElement }
        </appliance>.toString
      case _ => ""
    }
  }

  def createResultIQ(appliance: Appliance):ApplianceInstall = {
    val applianceInstall = new ApplianceInstall
    applianceInstall.setType(IQ.Type.RESULT)
    applianceInstall.setPacketID(getPacketID())
    applianceInstall.setFrom(getTo())
    applianceInstall.setTo(getFrom())
    applianceInstall.setAppliance(appliance)
    applianceInstall
   }
}

// vim: set ts=4 sw=4 et:
