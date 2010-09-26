
package com.beem.project.beem.service;

import static com.beem.project.beem.utils.CommonUtils.d;
import static com.beem.project.beem.utils.CommonUtils.getJIDWithoutResource;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;

import android.os.RemoteException;
import android.util.Log;
import ca.uwaterloo.crysp.otr.iface.OTRCallbacks;
import ca.uwaterloo.crysp.otr.iface.Policy;
import ca.uwaterloo.crysp.otr.iface.StringTLV;

import com.beem.project.beem.utils.CommonUtils;

public class OTRChatAdapter extends ChatAdapter {

  String ownerJid;

  public OTRChatAdapter(Chat chat, String ownerJid) {
    super(chat);
    this.ownerJid = getJIDWithoutResource(ownerJid);
  }

  @Override
  MsgListener createMessageListener() {
    return new OTRMessageListener(this);
  }

  @Override
  public void sendMessage(Message message) throws RemoteException {

    DefaultOTRCallbacks cb = new DefaultOTRCallbacks(this);
    OTRManager otrManager = OTRManager.getInstance();
    try {
      otrManager.getInterface().messageSending(ownerJid, message.getProtocol(),
          getJIDWithoutResource(this.getParticipant().getJID()), message.getBody(), null,
          Policy.FRAGMENT_SEND_ALL, cb);
    } catch (Exception e) {
      e.printStackTrace();
      d("OTRChatAdapter.sendMessage", message, e.getMessage(), "Sending message in plaintext");
      sendOtrMessage(message);
    }
  }

  public void sendOtrMessage(Message message) {
    org.jivesoftware.smack.packet.Message send = new org.jivesoftware.smack.packet.Message();
    send.setTo(message.getTo());
    d("OTRChatAdapter.sendOtrMessage", message.getTo(), message.getBody());

    send.setBody(message.getBody());
    send.setThread(message.getThread());
    send.setSubject(message.getSubject());
    send.setType(org.jivesoftware.smack.packet.Message.Type.chat);
    // TODO gerer les messages contenant des XMPPError
    // send.set
    try {
      mAdaptee.sendMessage(send);
      mMessages.add(message);
    } catch (XMPPException e) {
      e.printStackTrace();
    }
  }

  class OTRMessageListener extends MsgListener {

    private OTRChatAdapter sender;

    public OTRMessageListener(OTRChatAdapter sender) {
      this.sender = sender;
    }

    @Override
    public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {

      try {
        d("OTRMessageListener.processMessage (raw message - will send to OTR processor)",
            message.getBody());
        OTRCallbacks cb = new DefaultOTRCallbacks(sender);
        StringTLV stlv = OTRManager
            .getInstance()
            .getInterface()
            .messageReceiving(getJIDWithoutResource(message.getTo()), "xmpp",
                getJIDWithoutResource(message.getFrom()), message.getBody(), cb);
        if (stlv != null) {
          d("OTRMessageListener.processMessage (processed message for user)", stlv.msg);
          message.setBody(stlv.msg);
          super.processMessage(chat, message);
        } else {
          d("OTRMessageListener.processMessage (processed message not for user)",
              "OTR may have already responded");
        }
      } catch (Exception e) {
        d("OTRMessageListener.processMessage", "Could not receive message", e);
        Log.e(CommonUtils.TAG, "Could not receive message", e);
        message.setBody("Unable to receive message from user [" + e.getMessage() + "]");
        super.processMessage(chat, message);
      }
    }
  }

  private class DefaultOTRCallbacks extends AbstractOTRCallbacks {

    private OTRChatAdapter otrChatAdapter;

    public DefaultOTRCallbacks(OTRChatAdapter adapter) {
      this.otrChatAdapter = adapter;
    }

    @Override
    public void injectMessage(String accName, String prot, String rec, String msg) {
      d("DefaultOTRCallbacks.injectMessage (message to buddy)", accName, prot, rec, msg);
      Message message = new Message(getJIDWithoutResource(accName), Message.MSG_TYPE_CHAT);
      message.setBody(msg);
      otrChatAdapter.sendOtrMessage(message);
    }
  }
}
