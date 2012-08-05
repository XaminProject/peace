package ir.xamin.packet.reply

import ir.xamin.providers.ApplianceEnableProvider
import org.jivesoftware.smack.packet.IQ

class ApplianceEnable extends IQ {
  setType(IQ.Type.RESULT)

  def getChildElementXML:String = {
    val ns = ApplianceEnableProvider.namespace
    <appliance ns={ns} />.toString
  }
}

// vim: set ts=4 sw=4 et:
