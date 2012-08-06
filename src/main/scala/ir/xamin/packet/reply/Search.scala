package ir.xamin.packet.reply

import ir.xamin.Appliance
import scala.xml._
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.packet.{Packet, IQ}

class Search extends IQ {
  private var packages:MutableList[Appliance] = _

  setType(IQ.Type.RESULT)

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
      val tags = appliance.tags.flatMap { s => <tag>{s}</tag> }
      packagesTag += <appliance>
          <name>{name}</name>
          <version>{version}</version>
          <description>{description}</description>
          <author>{author}</author>
          <tags>{tags}</tags>
        </appliance>
    }
    <search xmlns="client:search:xamin">{ packagesTag }</search>.toString
  }
}

// vim: set ts=4 sw=4 et:
