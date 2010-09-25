
package com.beem.project.beem.service;

import static com.beem.project.beem.utils.L.d;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;

import android.os.RemoteException;
import android.util.Log;
import ca.uwaterloo.crysp.otr.iface.OTRCallbacks;
import ca.uwaterloo.crysp.otr.iface.Policy;
import ca.uwaterloo.crysp.otr.iface.StringTLV;

import com.beem.project.beem.utils.L;

public class OTRChatAdapter extends ChatAdapter {

  public OTRChatAdapter(Chat chat) {
    super(chat);
  }

  @Override
  MsgListener createMessageListener() {
    return new OTRMessageListener(this);
  }

  @Override
  public void sendMessage(Message message) throws RemoteException {
    
    SendingOTRCallbacks cb = new SendingOTRCallbacks(this, message);
    OTRManager otrManager = OTRManager.getInstance();
    try {
      otrManager.getInterface().getContext("arif.amirani@gmail.com/Adium04E7102", "xmpp", "arif.amirani@gmail.com/Adium04E7102");
      otrManager.getInterface().messageSending("arif.amirani@gmail.com/Adium04E7102", "xmpp", "arif.amirani@gmail.com/Adium04E7102",
          message.getBody(), otrManager.getTlvs(), Policy.FRAGMENT_SEND_ALL, cb);
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
      // TODO Auto-generated catch block
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
        d("OTRMessageListener.processMessage", "Received message: " + message.getBody());
        OTRCallbacks cb = new ReceivingOTRCallbacks(sender);
        StringTLV stlv = OTRManager.getInstance().getInterface().messageReceiving(message.getFrom(), "xmpp", message.getFrom(), message.getBody(), cb);
        if (stlv != null) {
          d("OTRMessageListener.processMessage", "Message from OTR (for user): " + stlv.msg);
        } else {
          d("OTRMessageListener.processMessage", "Message from OTR (not for user): Callback should be called");
        }
      } catch (Exception e) {
        d("OTRMessageListener.processMessage", "Could not receive message", e);
        Log.e(L.TAG, "Could not receive message", e);
      }
    }

    public void processOtrMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
      super.processMessage(chat, message);
    }

  }

  private class ReceivingOTRCallbacks extends AbstractOTRCallbacks {

    private final OTRChatAdapter sender;

    public ReceivingOTRCallbacks(OTRChatAdapter sender) {
      this.sender = sender;
    }

    @Override
    public void injectMessage(String accName, String prot, String rec, String msg) {
      d("ReceivingOTRCallbacks.injectMessage", accName, prot, rec, msg);
      try {
        Message message = new Message(accName, Message.MSG_TYPE_CHAT);
        message.setBody(msg);
        sender.sendMessage(message);
      } catch (RemoteException e) {
        d("ReceivingOTRCallbacks.injectMessage", "Could not send OTR message to " + accName, e.getMessage());
      }
    }
  }

  private class SendingOTRCallbacks extends AbstractOTRCallbacks {

    private final OTRChatAdapter otrChatAdapter;
    private final Message message;

    public SendingOTRCallbacks(OTRChatAdapter otrChatAdapter, Message message) {
      this.otrChatAdapter = otrChatAdapter;
      this.message = message;
    }

    @Override
    public void injectMessage(String accName, String prot, String rec, String msg) {
      d("SendingOTRCallbacks.injectMessage", accName, prot, rec, msg);
      message.setBody(msg);
      otrChatAdapter.sendOtrMessage(message);
    }
  }

}
