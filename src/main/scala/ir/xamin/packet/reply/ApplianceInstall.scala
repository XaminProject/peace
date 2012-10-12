package ir.xamin.packet.reply

import ir.xamin.providers.ApplianceInstallProvider
import ir.xamin.Appliance
import scala.xml._
import scala.collection.mutable.MutableList
import java.io.File
import java.net.URL
import org.jivesoftware.smack.packet.{IQ, Packet}

/** this class will be sent in reply to install request
 */
class ApplianceInstall extends IQ {
  private var base:String = _
  private var appliance:Appliance = _
  private var history = MutableList[Appliance]()

  // set type of iq
  setType(IQ.Type.RESULT)

  /** getter for appliance that user wants to install
   * @return an Appliance
   */
  def getAppliance = appliance

  /** setter for appliance that user wants to install
   * @param a an Appliance
   */
  def setAppliance(a: Appliance) = appliance = a

  /** getter for version history of appliance, based on base
   * version that user has currently installed
   * @return a list of Appliance objects (versions between base
   * and target)
   */
  def getHistory = history

  /** setter for version history of appliance
   * @param v a list of Appliances
   */
  def setHistory(v:MutableList[Appliance]) = history = v

  /** getter for base version
   * @return a string which is base version that user has installed
   */
  def getBase = base

  /** setter for base version
   * @param a string which is base version that user has installed
   */
  def setBase(v:String) = base = v

  /** creates inner xml of iq
   * @return a string which is inner xml of iq
   */
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
      case Appliance(n, v, d, u, a, e, t, c, m, s, ca, i) => {
        val tags = t.flatMap { s => <tag>{s}</tag> }
        val images = i.flatMap { s => <image>{s}</image> }
        <appliance xmlns={ ns }>
          <name>{ n }</name>
          <version>{ v }</version>
          <description>{ d }</description>
          <author>{ a }</author>
          <url>{ u }</url>
          <tags>{tags}</tags>
          <cpu>{c}</cpu>
          <memory>{m}</memory>
          <storage>{s}</storage>
          <category>{ca}</category>
          <images>{images}</images>
          { historyElement }
        </appliance>.toString
      }
      case _ => ""
    }
  }
}

// vim: set ts=4 sw=4 et:
