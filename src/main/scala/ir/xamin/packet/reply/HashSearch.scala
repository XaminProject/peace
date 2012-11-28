package ir.xamin.packet.reply

import ir.xamin.Appliance
import ir.xamin.providers.HashSearchProvider
import scala.xml._
import scala.collection.mutable.MutableList
import org.jivesoftware.smack.packet.{Packet, IQ}

class HashSearch extends IQ {
  private var packages:Map[String, Appliance] = _

  // set type of iq
  setType(IQ.Type.RESULT)

  /** setter for Appliances that matched the search
   * @param p a map of hash-string to appliances that matched the search
   */
  def setPackages(p:Map[String, Appliance]):Unit = packages = p

  /** getter for Appliances that matched the search
   * @return a map of hash-string to appliances that matched the search
   */
  def getPackages = packages

  /** creates the inner xml of packet
   */
  def getChildElementXML:String = {
    val ns = HashSearchProvider.namespace
    var packagesTag = MutableList[Elem]()
    for {
      thePair <- packages
    }{
      val theHash = thePair._1
      val appliance = thePair._2
      val name = appliance.name
      val version = appliance.version
      val description = appliance.description
      val author = appliance.author
      val category = appliance.category
      val icon = appliance.icon
      val creation = appliance.creation
      val home = appliance.home
      val tags = appliance.tags.flatMap { s => <tag>{s}</tag> }
      val images = for {
        m <- appliance.images
        path <- m.get("path")
        title <- m.get("title")
        description <- m.get("description")
      } yield <image><path>{path}</path><title>{title}</title><description>{description}</description></image>
      packagesTag += <appliance hash={theHash}>
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
    <search xmlns={ns}>{ packagesTag }</search>.toString
  }
}

// vim: set ts=4 sw=4 et:
