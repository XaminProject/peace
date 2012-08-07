package ir.xamin.packet.receive

import ir.xamin.packet.reply.{MarketRemove => ReplyMarketRemove}
import ir.xamin.providers.MarketInstallProvider
import scala.xml._
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.packet.{IQ, Packet}

/** a class which represents the stanza that market sends us
 * when user requests to remove an appliance from his/her
 * archipel
 */
class MarketRemove extends IQ {
  private var remove:MutableList[Tuple2[String, String]] = _
  private var archipel:String = _

  // set type of iq
  setType(IQ.Type.SET)

  /** getter for appliances that user requested to remove
   * @return a list of appliances to remove
   */
  def getRemove = remove

  /** setter for appliances that user requested to remove
   * @param v a list of appliances to be removed
   */
  def setRemove(v:MutableList[Tuple2[String, String]]) = remove = v

  /** getter for jid of target archipel
   * @return a string which is jid of target archipel
   */
  def getArchipel = archipel

  /** setter for jid of target archipel
   * @param v a string which is jid of target archipel
   */
  def setArchipel(v:String) = archipel = v

  /** creates inner xml of iq
   * @return a string which is going to be inner of iq
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
    <remove from={archipel} xmlns={ns}>
      {applianceElements}
    </remove>.toString
  }

  /** creates reply for request
   * @return the reply
   */
  def createResultIQ():ReplyMarketRemove = {
    val marketRemove = new ReplyMarketRemove
    marketRemove setPacketID getPacketID
    marketRemove setTo getArchipel
    marketRemove setRemove getRemove
    marketRemove
  }
}

// vim: set ts=4 sw=4 et:
