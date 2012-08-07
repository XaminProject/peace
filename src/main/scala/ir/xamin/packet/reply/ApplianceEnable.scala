package ir.xamin.packet.reply

import ir.xamin.providers.ApplianceEnableProvider
import org.jivesoftware.smack.packet.IQ

/** this class is an IQ that will be sent in reply to Enable
 * request of an appliance
 */
class ApplianceEnable extends IQ {
  // set type of iq
  setType(IQ.Type.RESULT)

  /** creates inner xml of iq
   * @return a string which is inner of iq
   */
  def getChildElementXML:String = {
    val ns = ApplianceEnableProvider.namespace
    <appliance ns={ns} />.toString
  }
}

// vim: set ts=4 sw=4 et:
