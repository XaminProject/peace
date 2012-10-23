package ir.xamin.packet.reply

import ir.xamin.Appliance
import scala.xml._
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.packet.{Packet, IQ}

class Search extends IQ {
  private var packages:MutableList[Appliance] = _

  // set type of iq
  setType(IQ.Type.RESULT)

  /** setter for Appliances that matched the search
   * @param p a list of Appliance that matched the result
   */
  def setPackages(p: MutableList[Appliance]):Unit = packages = p

  /** getter for Appliances that matched the search
   * @return a list of Appliance objects
   */
  def getPackages = packages

  /** creates the inner xml of packet
   */
  def getChildElementXML:String = {
    var packagesTag = MutableList[Elem]()
    for {
      appliance <- packages
    }{
      val name = appliance.name
      val version = appliance.version
      val description = appliance.description
      val author = appliance.author
      val category = appliance.category
      val icon = appliance.icon
      val creation = appliance.creation
      val home = appliance.home
      val tags = appliance.tags.flatMap { s => <tag>{s}</tag> }
      val images = appliance.images.flatMap { s => <image>{s}</image> }
      packagesTag += <appliance>
          <name>{name}</name>
          <version>{version}</version>
          <description>{description}</description>
          <author>{author}</author>
          <tags>{tags}</tags>
          <category>{category}</category>
          <images>{images}</images>
          <icon>{icon}</icon>
          <creation>{creation}</creation>
          <home>{home}</home>
        </appliance>
    }
    <search xmlns="client:search:xamin">{ packagesTag }</search>.toString
  }
}

// vim: set ts=4 sw=4 et:
