package ir.xamin.packet.receive

import ir.xamin.packet.reply.{MarketRemove => ReplyMarketRemove}
import ir.xamin.providers.MarketInstallProvider
import scala.xml._
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.packet.{IQ, Packet}

class MarketRemove extends IQ {
  private var remove:MutableList[Tuple2[String, String]] = _
  private var archipel:String = _

  setType(IQ.Type.SET)

  def getRemove = remove

  def setRemove(v:MutableList[Tuple2[String, String]]) = remove = v

  def getArchipel = archipel

  def setArchipel(v:String) = archipel = v

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

  def createResultIQ():ReplyMarketRemove = {
    val marketRemove = new ReplyMarketRemove
    marketRemove setPacketID getPacketID
    marketRemove setTo getArchipel
    marketRemove setRemove getRemove
    marketRemove
  }
}

// vim: set ts=4 sw=4 et:
