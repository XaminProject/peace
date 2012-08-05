package ir.xamin.packet.receive

import ir.xamin.packet.reply.{MarketInstall => ReplyMarketInstall}
import ir.xamin.providers.MarketInstallProvider
import scala.xml._
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.packet.{IQ, Packet}

class MarketInstall extends IQ {
  private var install:MutableList[Tuple2[String, String]] = _
  private var archipel:String = _

  setType(IQ.Type.SET)

  def getInstall = install

  def setInstall(v:MutableList[Tuple2[String, String]]) = install = v

  def getArchipel = archipel

  def setArchipel(v:String) = archipel = v

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

  def createResultIQ():ReplyMarketInstall = {
    val marketInstall = new ReplyMarketInstall
    marketInstall setPacketID getPacketID
    marketInstall setTo getArchipel
    marketInstall setInstall getInstall
    marketInstall
  }
}

// vim: set ts=4 sw=4 et:
