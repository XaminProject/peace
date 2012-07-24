package ir.xamin.packet

import ir.xamin.providers.ApplianceInstallProvider
import ir.xamin.Appliance
import scala.xml._
import scala.collection.mutable.MutableList
import java.io.File
import java.net.URL
import org.jivesoftware.smack.packet.{IQ, Packet}

class ApplianceInstall extends IQ {
  private var name:String = _
  private var version:String = _
  private var base:String = _
  private var appliance:Appliance = _
  private var history = MutableList[Appliance]()

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

  def setHistory(v:MutableList[Appliance]) = history = v

  def getChildElementXML:String = {
    val ns = ApplianceInstallProvider.namespace
    var historyElement:Elem = null
    val versionHistory = MutableList[Elem]()
    var previousVersion = base
    for(a <- history)
    {
      val v = a.version
      val u = a.url
      val url = new URL(u)
      val path = new File(url.getPath()).getParent // directory of the appliance
      val diffPath = path+"/"+a.name+"_"+previousVersion+"_to_"+v+".xdelta"
      val diffURL = new URL(url.getProtocol, url.getHost, url.getPort, diffPath)
      val xdelta = diffURL.toString
      versionHistory += <appliance version={v}>{xdelta}</appliance>
      previousVersion = a.version
    }
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
