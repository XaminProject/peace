package ir.xamin.packet.reply

import ir.xamin.providers.MarketInstallProvider
import scala.xml._
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.packet.{IQ, Packet}

class MarketRemove extends IQ {
  private var remove:MutableList[Tuple2[String, String]] = _

  setType(IQ.Type.RESULT)

  def getRemove = remove

  def setRemove(v:MutableList[Tuple2[String, String]]) = remove = v

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
