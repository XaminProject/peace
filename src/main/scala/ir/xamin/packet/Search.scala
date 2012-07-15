package ir.xamin.packet

import ir.xamin.Appliance
import scala.xml._
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.packet.{Packet, IQ}

class Search extends IQ {
  private var query:String = _
  private var packages:MutableList[Appliance] = _

  setType(IQ.Type.GET)

  def getQuery = query

  def setQuery(value: String):Unit = query = value

  def setPackages(p: MutableList[Appliance]):Unit = packages = p

  def getPackages = packages

  def getChildElementXML:String = {
    var packagesTag = MutableList[Elem]()
    for {
      appliance <- packages
    }{
      val name = appliance.name
      val version = appliance.version
      val description = appliance.description
      val author = appliance.author
      packagesTag += <appliance>
          <name>{name}</name>
          <version>{version}</version>
          <description>{description}</description>
          <author>{author}</author>
        </appliance>
    }
    val xml = <search xmlns="client:search:xamin">{ packagesTag }</search>
    return xml.toString
  }

  def createResultIQ(packages: MutableList[Appliance]):Packet = {
    val search = new Search
    search.setType(IQ.Type.RESULT)
    search.setPacketID(getPacketID())
    search.setFrom(getTo())
    search.setTo(getFrom())
    search.setPackages(packages)
    search
  }
}

// vim: set ts=4 sw=4 et:
