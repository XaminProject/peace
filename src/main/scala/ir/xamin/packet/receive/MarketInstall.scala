package ir.xamin.packet.receive

import ir.xamin.packet.reply.{MarketInstall => ReplyMarketInstall}
import ir.xamin.providers.MarketInstallProvider
import scala.xml._
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.packet.{IQ, Packet}

/** a class which represents the stanza that market sends us
 * when user requests to install an appliance his/her archipel
 */
class MarketInstall extends IQ {
  private var install:MutableList[Tuple2[String, String]] = _
  private var archipel:String = _

  // set type of iq
  setType(IQ.Type.SET)

  /** getter for appliances that user requested to install
   * @return a list of appliances that user requested to be
   * installed on archipel
   */
  def getInstall = install

  /** setter for appliances that user requested to install
   * @param v a list of appliances
   */
  def setInstall(v:MutableList[Tuple2[String, String]]) = install = v

  /** getter for jid of target archipel
   * @return a string which is jid of target archipel
   */
  def getArchipel = archipel

  /** setter for jid of target archipel
   * @return a string which is jid of target archipel
   */
  def setArchipel(v:String) = archipel = v

  /** creates inner xml of iq
   * @return a string which is inner of iq
   */
  def getChildElementXML:String = {
    val ns = MarketInstallProvider.namespace
    var applianceElements = MutableList[Elem]()
    if(install != null)
      for(appliance <- install) {
        val name = appliance._1
        val version = appliance._2
        applianceElements += <appliance version={version}>{name}</appliance>
      }
    <install to={archipel} xmlns={ns}>
      {applianceElements}
    </install>.toString
  }

  /** creates reply to request
   * @return the reply
   */
  def createResultIQ():ReplyMarketInstall = {
    val marketInstall = new ReplyMarketInstall
    marketInstall setPacketID getPacketID
    marketInstall setTo getArchipel
    marketInstall setInstall getInstall
    marketInstall
  }
}

// vim: set ts=4 sw=4 et:
