package ir.xamin.packet.receive

import ir.xamin.packet.reply.{ApplianceRemoved => ReplyApplianceRemoved}
import ir.xamin.providers.ApplianceRemovedProvider
import ir.xamin.Appliance
import scala.xml._
import org.jivesoftware.smack.packet.{IQ, Packet}

/** a class for appliance removed packet
 *
 * after removing an appliance, archipel notifies peace about
 * the removed appliance via this IQ packet
 */
class ApplianceRemoved extends IQ {
  private var name:String = _
  private var version:String = _

  // set type of IQ
  setType(IQ.Type.SET)

  /** getter for name of appliance
   * @return a string which is name of appliance that has been
   * removed
   */
  def getName = name

  /** setter for name of appliance
   * @param value the name of appliance that removed
   */
  def setName(value: String):Unit = name = value

  /** getter for version of appliance
   * @return a string which is version of appliance that has been
   * removed
   */
  def getVersion = version

  /** setter for version of appliance
   * @param value the version of removed appliance
   */
  def setVersion(value: String):Unit = version = value

  /** creates innner xml of IQ
   * @return a string which is inner xml of iq
   */
  def getChildElementXML:String = {
    val ns = ApplianceRemovedProvider.namespace
    <appliance xmlns={ ns }>
      <name>{name}</name>
      <version>{version}</version>
    </appliance>.toString
  }

  /** creates response to this request
   * @param appliance the removed applinance
   * @return the response
   */
  def createResultIQ(appliance: Appliance):ReplyApplianceRemoved = {
    val applianceRemoved = new ReplyApplianceRemoved
    applianceRemoved setPacketID getPacketID
    applianceRemoved setFrom getTo
    applianceRemoved setTo getFrom
    applianceRemoved
  }
}

// vim: set ts=4 sw=4 et:
