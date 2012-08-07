package ir.xamin.packet.receive

import ir.xamin.packet.reply.{ApplianceInstall => ReplyApplianceInstall}
import ir.xamin.providers.ApplianceInstallProvider
import ir.xamin.Appliance
import scala.xml._
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.packet.{IQ, Packet}

/** this class represents an IQ stanza which ables user to
 * install an appliance
 */
class ApplianceInstall extends IQ {
  private var name:String = _
  private var version:String = _
  private var base:String = _

  // set the type of iq
  setType(IQ.Type.SET)

  /** getter for name of appliance that user wants to install
   * @return a string which is name of appliance
   */
  def getName = name

  /** setter for name of appliance that user wants to install
   * @param v a string which is name of appliance
   */
  def setName(v:String) = name = v

  /** getter for version of appliance that user wants to install
   * @return string version of appliance
   */
  def getVersion = version

  /** setter for version of appliance that user wants to install
   * @param v a string which is version of appliance
   */
  def setVersion(v:String) = version = v

  /** getter for base version that user currently has installed
   * by using base we can give bunch of xdelta files to user so
   * they don't need to download whole xvm2 file for update
   * @return a string which is base version
   */
  def getBase = base

  /** setter for base version that user currently has installed
   * @param v a string which is version of appliance that user has
   * already installed
   */
  def setBase(v:String) = base = v

  /** creates inner xml of iq
   * @return a string which is inner representation of iq
   */
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

  /** creates an iq response to request
   * @param appliance the appliance that user requested to install
   * @return the response iq
   */
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
