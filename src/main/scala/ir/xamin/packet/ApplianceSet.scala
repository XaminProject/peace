package ir.xamin.packet

import ir.xamin.providers.ApplianceSetProvider
import ir.xamin.Appliance
import scala.xml._
import org.jivesoftware.smack.packet.IQ

class ApplianceSet extends IQ {
  private var name:String = _
  private var description:String = _
  private var version:String = _
  private var url:String = _
  private var author:String = _

  setType(IQ.Type.SET)

  def getName = name
  def setName(v:String) = name = v
  def getDescription = description
  def setDescription(v:String) = description = v
  def getVersion = version
  def setVersion(v:String) = version = v
  def getURL = url
  def setURL(v:String) = url = v
  def getAuthor = author
  def setAuthor(v:String) = author = v
  def getChildElementXML:String = {
    val ns = ApplianceSetProvider.namespace
    return <package xmlns={ ns }>
        <name>{ name }</name>
        <description>{ description }</description>
        <version>{ version }</version>
        <url>{ url }</url>
        <author>{ author }</author>
      </package>.toString
  }
}

// vim: set ts=4 sw=4 et:
