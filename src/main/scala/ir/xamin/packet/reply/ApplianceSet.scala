package ir.xamin.packet.reply

import org.jivesoftware.smack.packet.IQ

/** this class will be sent back to rms as reply of creating new appliance
 */
class ApplianceSet extends IQ {
  // set type of iq
  setType(IQ.Type.RESULT)

  /** creates inner xml of iq
   */
  def getChildElementXML = ""
}

// vim: set ts=4 sw=4 et:
