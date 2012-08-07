package ir.xamin.packet.receive

import ir.xamin.packet.reply.{ApplianceEnable => ReplyApplianceEnable}
import ir.xamin.providers.ApplianceEnableProvider
import ir.xamin.Appliance
import scala.xml._
import org.jivesoftware.smack.packet.{IQ, Packet}

/** a class for enabling an appliance
 *
 * after creating an appliance in RMS it tells peace to enable
 * appliance which to say so send us an IQ stanza that this
 * class represents
 */
class ApplianceEnable extends IQ {
  private var name:String = _
  private var version:String = _

  // set type of IQ
  setType(IQ.Type.GET)

  /** getter for name of appliance
   * @return a string which is name of appliance that has been
   * requested to get enabled
   */
  def getName = name

  /** setter for name of appliance
   * @param value the name of appliance that should be enabled
   */
  def setName(value: String):Unit = name = value

  /** getter for version of appliance
   * @return a string which is version of appliance that has been
   * requested to get enabled
   */
  def getVersion = version

  /** setter for version of appliance
   * @param value the version of appliance that should be enabled
   */
  def setVersion(value: String):Unit = version = value

  /** creates innner xml of IQ
   */
  def getChildElementXML:String = {
    val ns = ApplianceEnableProvider.namespace
    <appliance xmlns={ ns }>
      <name>{name}</name>
      <version>{version}</version>
    </appliance>.toString
  }

  /** creates response to this request
   * @param the enabled applinance
   * @return the response
   */
  def createResultIQ(appliance: Appliance):ReplyApplianceEnable = {
    val applianceEnable = new ReplyApplianceEnable
    applianceEnable setPacketID getPacketID
    applianceEnable setFrom getTo
    applianceEnable setTo getFrom
    applianceEnable
  }
}

// vim: set ts=4 sw=4 et:
