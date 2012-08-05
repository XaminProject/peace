package ir.xamin.packet.reply

import ir.xamin.providers.ApplianceInstallProvider
import ir.xamin.Appliance
import scala.xml._
import scala.collection.mutable.MutableList
import java.io.File
import java.net.URL
import org.jivesoftware.smack.packet.{IQ, Packet}

class ApplianceInstall extends IQ {
  private var base:String = _
  private var appliance:Appliance = _
  private var history = MutableList[Appliance]()

  setType(IQ.Type.RESULT)

  def getAppliance = appliance

  def setAppliance(a: Appliance) = appliance = a

  def getHistory = history

  def setHistory(v:MutableList[Appliance]) = history = v

  def getBase = base

  def setBase(v:String) = base = v

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
      // the path is like http://hostname/path/to/images/some-hash-name/
      // and our xdelta is in same path called old-version_TO_new-version.xdelta
      val diffPath = path+"/"+previousVersion+"_TO_"+v+".xdelta"
      val diffURL = new URL(url.getProtocol, url.getHost, url.getPort, diffPath)
      val xdelta = diffURL.toString
      versionHistory += <appliance version={v}>{xdelta}</appliance>
      previousVersion = a.version
    }
    if(!versionHistory.isEmpty)
      historyElement = <history>{ versionHistory }</history>
    appliance match {
      case Appliance(n, v, d, u, a, e) => <appliance xmlns={ ns }>
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
}

// vim: set ts=4 sw=4 et:
