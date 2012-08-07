package ir.xamin.packet.receive

import ir.xamin.packet.reply.{ApplianceGet => ReplyApplianceGet}
import ir.xamin.providers.ApplianceGetProvider
import ir.xamin.Appliance
import scala.xml._
import org.jivesoftware.smack.packet.{IQ, Packet}

/** a class that ables users to get information about an appliance
 */
class ApplianceGet extends IQ {
  private var name:String = _
  private var version:String = _

  // set type of iq
  setType(IQ.Type.GET)

  /** getter for name of appliance
   * @return a string which is name of appliance
   */
  def getName = name

  /** setter for name of appliance
   * @param value name of appliance
   */
  def setName(value: String):Unit = name = value

  /** getter for version of appliance
   * @return a string which is version of appliance
   */
  def getVersion = version

  /** setter for version of appliance
   * @param value version of appliance
   */
  def setVersion(value: String):Unit = version = value

  /** creates inner xml of iq
   * @return a string which is inner xml of iq
   */
  def getChildElementXML:String = {
    val ns = ApplianceGetProvider.namespace
    <appliance xmlns={ ns }>
      <name>{name}</name>
      <version>{version}</version>
    </appliance>.toString
  }

  /** creates response iq of request
   * @param appliance the appliance that user requested to get info of
   * @return the response
   */
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
