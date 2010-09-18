package com.beem.project.beem.service;
import android.util.Log;
import ca.uwaterloo.crysp.otr.iface.OTRCallbacks;
import ca.uwaterloo.crysp.otr.iface.OTRContext;
import ca.uwaterloo.crysp.otr.iface.OTRInterface;
import ca.uwaterloo.crysp.otr.iface.Policy;
public class AbstractOTRCallbacks implements OTRCallbacks {
  
  private static final String TAG = AbstractOTRCallbacks.class.getName();
  
  public int getOtrPolicy(OTRContext conn) {
    return Policy.DEFAULT;
  }
  
  public int isLoggedIn(String accountname, String protocol, String recipient) {
    d("isLoggedIn", accountname, protocol, recipient);
    return -1;
  }
  
  public void injectMessage(String accName, String prot, String rec, String msg) {
    d("injectMessage", accName, prot, rec, msg);
  }
  
  public void updateContextList() {
    d("updateContextList");
  }
  
  public void newFingerprint(OTRInterface us, String accountname, String protocol, String username,
      byte[] fingerprint) {
    d("newFingerprint", us.toString(), accountname, protocol, username, new String(fingerprint));
  }
  
  public void writeFingerprints() {
    d("writeFingerprints");
  }
  
  public void goneSecure(OTRContext context) {
    d("goneSecure", context.toString());
  }
  
  public void stillSecure(OTRContext context, int is_reply) {
    d("stillSecure", context.toString(), Integer.toString(is_reply));
  }
  
  public int maxMessageSize(OTRContext context) {
    d("maxMessageSize", context.toString());
    return 1000;
  }
  
  public String errorMessage(OTRContext context, int err_code) {
    d("errorMessage", context.toString(), Integer.toString(err_code));
    return null;
  }
  
  public void handleSmpEvent(int smp_event, OTRContext context, int progress_percent,
      String question) {
    d("handleSmpEvent", Integer.toString(smp_event), Integer.toString(progress_percent), question);
  }
  
  public void handleMsgEvent(int msg_event, OTRContext context, String message) {
    d("handleMsgEvent", message);
  }
  
  void d(String from, String...args) {
    StringBuffer sb = new StringBuffer();
    sb.append(from).append(": ");
    for (String s : args) {
      sb.append(" [").append(s).append(']');
    }
    Log.d(TAG, sb.toString());
  }
}
