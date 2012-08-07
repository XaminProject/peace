package ir.xamin.packet.reply

import ir.xamin.providers.MarketInstallProvider
import scala.xml._
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.packet.{IQ, Packet}

/** this class will be used to send remove packet to target archipel
 */
class MarketRemove extends IQ {
  private var remove:MutableList[Tuple2[String, String]] = _

  // set type of iq
  setType(IQ.Type.RESULT)

  /** getter for list of appliances that should be removed
   * @return a list of (name, version) to be removed
   */
  def getRemove = remove

  /** setter for list of appliances that should be removed
   * @param a list of (name, version) to be removed
   */
  def setRemove(v:MutableList[Tuple2[String, String]]) = remove = v

  /** creates inner xml of packet
   * @return a string which is inner xml of <iq />
   */
  def getChildElementXML:String = {
    val ns = MarketInstallProvider.namespace
    var applianceElements = MutableList[Elem]()
    if(remove != null)
      for(appliance <- remove) {
        val name = appliance._1
        val version = appliance._2
        applianceElements += <appliance version={version}>{name}</appliance>
      }
    <query xmlns={ns}>
      <archipel action="remove">
        {applianceElements}
      </archipel>
    </query>.toString
  }
}

// vim: set ts=4 sw=4 et:
