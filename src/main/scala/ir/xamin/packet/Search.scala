package ir.xamin.packet

import scala.xml._
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.packet.{Packet, IQ}

class Search extends IQ {
  private var query:String = _
  private var packages:Option[List[Option[String]]] = _

  setType(IQ.Type.GET)

  def getQuery = query

  def setQuery(value: String):Unit = query = value

  def setPackages(p: Option[List[Option[String]]]):Unit = packages = p

  def getPackages = packages

  def getChildElementXML:String = {
    if(packages.isEmpty){
      val search = <search xmlns="client:search:xamin" query="{ query }" />
      return search.toString
    }
    var packagesTag = MutableList[Elem]()
    for {
      pack <- packages.get
    }{
      val name = new String(pack.get).substring(10)
      packagesTag += <package>{ name }</package>
    }
    val xml = <search xmlns="client:search:xamin">{ packagesTag }</search>
    return xml.toString
  }

  def createResultIQ(packages: Option[List[Option[String]]]):Packet = {
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
