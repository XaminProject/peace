package ir.xamin.packet.reply

import ir.xamin.providers.ApplianceRemovedProvider
import org.jivesoftware.smack.packet.IQ

/** this class is an IQ that will be sent in reply to Appliance
 * Removed notify
 */
class ApplianceRemoved extends IQ {
  // set type of iq
  setType(IQ.Type.RESULT)

  /** creates inner xml of iq
   * @return a string which is inner of iq
   */
  def getChildElementXML:String = {
    ""
  }
}

// vim: set ts=4 sw=4 et:
