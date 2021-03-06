package ir.xamin.packet

import scala.xml._
import org.jivesoftware.smack.XMPPConnection
import org.jivesoftware.smack.packet.Packet

/** this class creates the packet that we need to send to xmpp server
 * to be able to subscribe another jid to a pubsub
 */
class OwnerBehalfSubscribe(xmpp: XMPPConnection, aJid:String, aNode:String) extends Packet {
  protected val to:String = "pubsub."+xmpp.getServiceName()
  protected val node = aNode
  protected val jid = aJid

  /** creates inner xml of packet
   */
  override def toXML:String = {
    <iq type='set' to={to}>
      <pubsub xmlns='http://jabber.org/protocol/pubsub#owner'>
        <subscriptions node={node}>
          <subscription jid={jid} subscription='subscribed'/>
        </subscriptions>
      </pubsub>
    </iq>.toString
  }
}

// vim: set ts=4 sw=4 et:
