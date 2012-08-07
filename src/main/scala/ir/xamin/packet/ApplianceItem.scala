package ir.xamin.packet

import scala.xml._
import org.jivesoftware.smackx.pubsub.Item

/** this class will be used to provide inner xml of pubsub item
 * that will be published
 */
case class ApplianceItem(version:String) extends Item()
{
  /** creates the inner xml of item
   */
  override def toXML = <item>
      <appliance xmlns="appliance:update:xamin">
        <version>{version}</version>
      </appliance>
    </item>.toString
}

// vim: set ts=4 sw=4 et:
