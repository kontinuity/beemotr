package com.beem.project.beem.service;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;

import com.beem.project.beem.service.ChatAdapter.MsgListener;

import android.os.RemoteException;
import android.util.Log;
import ca.uwaterloo.crysp.otr.UserState;
import ca.uwaterloo.crysp.otr.crypt.jca.JCAProvider;
import ca.uwaterloo.crysp.otr.iface.OTRCallbacks;
import ca.uwaterloo.crysp.otr.iface.OTRInterface;
import ca.uwaterloo.crysp.otr.iface.Policy;
import ca.uwaterloo.crysp.otr.iface.StringTLV;
import ca.uwaterloo.crysp.otr.message.FragmentMessage;

public class OTRChatAdapter extends ChatAdapter {
  
  private static final String TAG = "OTRChatAdapter";
  
  public OTRChatAdapter(Chat chat) {
    super(chat);
  }
  
  @Override
  MsgListener createMessageListener() {
    return new OTRMessageListener();
  }  

  @Override
  public void sendMessage(Message message) throws RemoteException {
    SendingOTRCallbacks cb = new SendingOTRCallbacks(this, message);
    try {
      OTRManager.getInstance().getUserState().messageSending(message.getTo(), "xmpp", message.getTo(), message.getBody(), null, Policy.FRAGMENT_SEND_ALL, cb);
    } catch (Exception e) {
      e.printStackTrace();
      Log.e(TAG, "Could not send message", e);
      sendOtrMessage(message);
    }
  }

  public void sendOtrMessage(Message message) {
    org.jivesoftware.smack.packet.Message send = new org.jivesoftware.smack.packet.Message();
    send.setTo(message.getTo());
    Log.w(TAG, "message to " + message.getTo());
    
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

    @Override
    public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
      
      try {
        OTRCallbacks cb = new ReceivingOTRCallbacks(chat, message, this);
        StringTLV stlv = OTRManager.getInstance().getUserState()
            .messageReceiving(message.getFrom(), "xmpp", message.getFrom(), message.getBody(), cb);
        if (stlv != null) {
          Log.d(TAG, "Message from OTR (not for user): " + stlv.msg);
        }
      } catch (Exception e) {
        Log.e(TAG, "Could not receive message", e);
      }      
    }
    
    public void processOtrMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
      super.processMessage(chat, message);
    }
    
  }
  
  private class ReceivingOTRCallbacks extends AbstractOTRCallbacks {
    
    private final Chat chat;
    private final org.jivesoftware.smack.packet.Message message;
    private final OTRMessageListener listener;
    
    public ReceivingOTRCallbacks(Chat chat, org.jivesoftware.smack.packet.Message message, OTRMessageListener listener) {
      this.chat = chat;
      this.message = message;
      this.listener = listener;
    }
    
    @Override
    public void injectMessage(String accName, String prot, String rec, String msg) {
      d("injectMessage", accName, prot, rec, msg);
      message.setBody(msg);
      listener.processOtrMessage(chat, message);
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
      d("injectMessage", accName, prot, rec, msg);
      message.setBody(msg);
      otrChatAdapter.sendOtrMessage(message);
    }
  }
  
}
