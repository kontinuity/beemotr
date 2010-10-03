
package com.beem.project.beem.service;

import static com.beem.project.beem.utils.CommonUtils.d;
import static com.beem.project.beem.utils.CommonUtils.encode;
import static com.beem.project.beem.utils.CommonUtils.getHumanMessageEvent;
import static com.beem.project.beem.utils.CommonUtils.getHumanSMPEvent;
import ca.uwaterloo.crysp.otr.iface.OTRCallbacks;
import ca.uwaterloo.crysp.otr.iface.OTRContext;
import ca.uwaterloo.crysp.otr.iface.OTRInterface;
import ca.uwaterloo.crysp.otr.iface.Policy;

public class AbstractOTRCallbacks implements OTRCallbacks {

  public int getOtrPolicy(OTRContext conn) {
    return Policy.DEFAULT;
  }

  public int isLoggedIn(String accountname, String protocol, String recipient) {
    d("AbstractOTRCallbacks.isLoggedIn", accountname, protocol, recipient);
    return 1;
  }

  public void injectMessage(String accName, String prot, String rec, String msg) {
    d("AbstractOTRCallbacks.injectMessage", accName, prot, rec, msg);
  }

  public void updateContextList() {
    d("AbstractOTRCallbacks.updateContextList");
  }

  public void newFingerprint(OTRInterface us, String accountname, String protocol, String username,
      byte[] fingerprint) {
    d("AbstractOTRCallbacks.newFingerprint", us.toString(), accountname, protocol, username,
        encode(fingerprint));
  }

  public void writeFingerprints() {
    d("AbstractOTRCallbacks.writeFingerprints");
  }

  public void goneSecure(OTRContext context) {
    d("AbstractOTRCallbacks.goneSecure", context.toString());
  }

  public void stillSecure(OTRContext context, int is_reply) {
    d("AbstractOTRCallbacks.stillSecure", context.toString(), Integer.toString(is_reply));
  }

  public int maxMessageSize(OTRContext context) {
    d("AbstractOTRCallbacks.maxMessageSize", context.toString());
    return 1000;
  }

  public String errorMessage(OTRContext context, int err_code) {
    d("AbstractOTRCallbacks.errorMessage", context.toString(), Integer.toString(err_code));
    return null;
  }

  public void handleSmpEvent(int smp_event, OTRContext context, int progress_percent,
      String question) {
    d("AbstractOTRCallbacks.handleSmpEvent", getHumanSMPEvent(smp_event),
        Integer.toString(progress_percent), question);
  }

  public void handleMsgEvent(int msg_event, OTRContext context, String message) {
    d("AbstractOTRCallbacks.handleMsgEvent", getHumanMessageEvent(msg_event), message);
  }
}
