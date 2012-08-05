package ir.xamin.packet.reply

import org.jivesoftware.smack.packet.IQ

class ApplianceSet extends IQ {
  setType(IQ.Type.RESULT)

  def getChildElementXML = ""
}

// vim: set ts=4 sw=4 et:
