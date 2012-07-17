package ir.xamin.packet

import scala.xml._
import org.jivesoftware.smackx.pubsub.Item

case class ApplianceItem(version:String) extends Item()
{
  override def toXML = <item>
      <appliance xmlns="appliance:update:xamin">
        <version>{version}</version>
      </appliance>
    </item>.toString
}

// vim: set ts=4 sw=4 et:
