package ir.xamin.packet.receive

import ir.xamin.packet.reply.{Search => ReplySearch}
import ir.xamin.providers.SearchProvider
import ir.xamin.Appliance
import scala.xml._
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.packet.{Packet, IQ}

class Search extends IQ {
  private var query:String = _

  setType(IQ.Type.GET)

  def getQuery = query

  def setQuery(value: String):Unit = query = value

  def getChildElementXML:String = {
    val ns = SearchProvider.namespace
    <search xmlns={ns} query={query}/>.toString
  }

  def createResultIQ(packages: MutableList[Appliance]):ReplySearch = {
    val search = new ReplySearch
    search setPacketID getPacketID
    search setFrom getTo
    search setTo getFrom
    search setPackages packages
    search
  }
}

// vim: set ts=4 sw=4 et:
