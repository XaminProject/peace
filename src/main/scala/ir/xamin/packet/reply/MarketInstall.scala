package ir.xamin.packet.reply

import ir.xamin.providers.MarketInstallProvider
import scala.xml._
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.packet.{IQ, Packet}

/** this class will be used for sending install request to archipel
 */
class MarketInstall extends IQ {
  private var install:MutableList[Tuple2[String, String]] = _

  // set type of iq
  setType(IQ.Type.RESULT)

  /** getter for list of appliances that should be installed
   * @return a list of (name, version) to be installed on target archipel
   */
  def getInstall = install

  /** setter for list of appliances that should be installed
   * @param v a list of (name, version) to be installed on target archipel
   */
  def setInstall(v:MutableList[Tuple2[String, String]]) = install = v

  /** creates the inner xml of iq
   * @return a string which is inner xml of iq
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
    <query xmlns={ns}>
      <archipel action="install">
        {applianceElements}
      </archipel>
    </query>.toString
  }
}

// vim: set ts=4 sw=4 et:
