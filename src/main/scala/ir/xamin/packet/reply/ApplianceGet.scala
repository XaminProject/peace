package ir.xamin.packet.reply

import ir.xamin.providers.ApplianceGetProvider
import ir.xamin.Appliance
import scala.xml._
import org.jivesoftware.smack.packet.{IQ, Packet}

/** this class will be used to create reply of appliance
 * info request
 */
class ApplianceGet extends IQ {
  private var appliance:Appliance = _

  // set type of iq
  setType(IQ.Type.RESULT)

  /** getter for appliance which is going to be sent back as response
   * @return an Appliance which is going to be sent back to user
   */
  def getAppliance = appliance

  /** setter for appliance which is going to be sent back as response
   * @param a an Appliance which is going  to be sent back to user
   */
  def setAppliance(a: Appliance) = appliance = a

  /** creates the inner xml of iq
   * @return a String which is inner xml of iq
   */
  def getChildElementXML:String = {
    val ns = ApplianceGetProvider.namespace
    appliance match {
      case Appliance(n, v, d, u, a, e, t, c, m, s, ca) => {
        val tags = t.flatMap { s => <tag>{s}</tag> }
        <appliance xmlns={ ns }>
          <name>{ n }</name>
          <version>{ v }</version>
          <description>{ d }</description>
          <author>{ a }</author>
          <tags>{tags}</tags>
          <cpu>{c}</cpu>
          <memory>{m}</memory>
          <storage>{s}</storage>
          <category>{ca}</category>
        </appliance>.toString
      }
      case _ => ""
    }
  }
}

// vim: set ts=4 sw=4 et:
