package ir.xamin.packet.receive

import ir.xamin.packet.reply.{ApplianceSet => ReplyApplianceSet}
import ir.xamin.providers.ApplianceSetProvider
import ir.xamin.Appliance
import scala.xml._
import org.jivesoftware.smack.packet.IQ

/** this class ables rms to tell peace about new appliances
 * and new version of them
 */
class ApplianceSet extends IQ {
  private var name:String = _
  private var description:String = _
  private var version:String = _
  private var url:String = _
  private var author:String = _
  private var tags:List[String] = _
  private var cpu:Int = _
  private var memory:Int = _
  private var storage:Int = _

  // set type of iq
  setType(IQ.Type.SET)

  /** getter for name of appliance that will be stored
   * @return a string which is name of appliance
   */
  def getName = name

  /** setter for name of appliance that will be stored
   * @param v a string that is name of appliance
   */
  def setName(v:String) = name = v

  /** getter for description of appliance that will be stored
   * @return a string which is name of appliance
   */
  def getDescription = description

  /** setter for description of appliance that will be stored
   * @param v a string which is description of appliance
   */
  def setDescription(v:String) = description = v

  /** getter for version of appliance that will be stored
   * @return a string which is version of appliance
   */
  def getVersion = version

  /** setter for version of appliance that will be stored
   * @param v a string which is version of appliance
   */
  def setVersion(v:String) = version = v

  /** getter for url of xvm2 of appliance that will be stored
   * @return a string which is url of xvm2 file of appliance
   */
  def getURL = url

  /** setter for url of xvm2 of appliance that will be stored
   * @param v a string which is url of xmv2 file of appliance
   */
  def setURL(v:String) = url = v

  /** getter for author of appliance that will be stored
   * @return a string which is author of appliance
   */
  def getAuthor = author

  /** setter for author of appliance that will be stored
   * @param v a string which is author of appliance
   */
  def setAuthor(v:String) = author = v

  /** getter for tags of appliance that will be stored
   * @return a list of tags
   */
  def getTags = tags

  /** setter for tags of appliance that will be stored
   * @param v the list of tags
   */
  def setTags(v:List[String]) = tags = v

  /** getter for number of logical CPUs of appliance that
   * gonna be stored
   * @return the number of cpu
   */
  def getCPU = cpu

  /** setter for number of logical CPUs of appliance that
   * gonna be stored
   * @param v an Integer for number of CPUs
   */
  def setCPU(v:Int) = cpu = v

  /** getter for memory of appliance
   * @return the amount of memory for appliance
   */
  def getMemory = memory

  /** setter for memory of appliance
   * @param v the amount of memory for appliance
   */
  def setMemory(v:Int) = memory = v

  /** getter for storage of appliance
   * @return the storage of appliance
   */
  def getStorage = storage

  /** setter for storage of appliance
   * @param v the storage of appliance
   */
  def setStorage(v:Int) = storage = v

  /** the inner xml of iq
   * @return a string which is going to be inner xml of iq
   */
  def getChildElementXML:String = {
    val ns = ApplianceSetProvider.namespace
    val _tags = tags.flatMap { s => <tag>{s}</tag> }
    <package xmlns={ ns }>
      <name>{ name }</name>
      <description>{ description }</description>
      <version>{ version }</version>
      <url>{ url }</url>
      <author>{ author }</author>
      <cpu>{cpu}</cpu>
      <memory>{memory}</memory>
      <storage>{storage}</storage>
      <tags>{_tags}</tags>
    </package>.toString
  }

  /** creates reply to request
   * @return the reply
   */
  def buildReply:ReplyApplianceSet = {
    val applianceSet = new ReplyApplianceSet
    applianceSet setPacketID getPacketID
    applianceSet setFrom getTo
    applianceSet setTo getFrom
    applianceSet
  }
}

// vim: set ts=4 sw=4 et:
