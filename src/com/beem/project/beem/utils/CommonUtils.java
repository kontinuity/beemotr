package com.beem.project.beem.utils;

import org.jivesoftware.smack.util.Base64;

import android.content.SharedPreferences;
import android.util.Log;
import ca.uwaterloo.crysp.otr.MsgState;
import ca.uwaterloo.crysp.otr.Proto;
import ca.uwaterloo.crysp.otr.iface.OTRCallbacks;

import com.beem.project.beem.BeemApplication;

public class CommonUtils {
  
  public static final String TAG = "OTRBeemChat";
  
  public static void d(String from, Object... params) {
    try {
      Log.d(TAG, generateLogMessage(from, params));
    } catch(Exception e) {
      System.out.println("[" + TAG + "] - " + generateLogMessage(from, params));
    }
  }

  public static void e(String from, Throwable e, Object... params) {
    try {
      Log.e(TAG, generateLogMessage(from, params), e);
    } catch(Exception ex) {
      System.out.println("[" + TAG + "] - " + generateLogMessage(from, params));
    }
  }
  
  private static String generateLogMessage(String from, Object... params) {
    StringBuilder sb = new StringBuilder("[" + from + "] [");
    for (Object param : params) {
      sb.append('{').append(param).append('}');
    }
    sb.append(']');
    return sb.toString();
  }
  
  public static String getHumanMessageState(int state) {
    switch (state) {
      case MsgState.ST_UNENCRYPTED:
        return "ST_UNENCRYPTED"; 
      case MsgState.ST_ENCRYPTED:
        return "ST_ENCRYPTED";
      case MsgState.ST_FINISHED:
        return "ST_FINISHED";
    }
    return "Unknown";
  }
  
  public static String getHumanFragmentState(int state) {
    switch (state) {
      case Proto.FRAGMENT_UNFRAGMENTED:
        return "FRAGMENT_UNFRAGMENTED";
      case Proto.FRAGMENT_INCOMPLETE:
        return "FRAGMENT_INCOMPLETE";
      case Proto.FRAGMENT_COMPLETE:
        return "FRAGMENT_COMPLETE";
    }    
    return "Unknown";
  }
  
  public static String getHumanMessageEvent(int event) {
    switch (event) {
      case OTRCallbacks.OTRL_MSGEVENT_NONE:
        return "OTRL_MSGEVENT_NONE";
      case OTRCallbacks.OTRL_MSGEVENT_ENCRYPTION_REQUIRED:
        return "OTRL_MSGEVENT_ENCRYPTION_REQUIRED";
      case OTRCallbacks.OTRL_MSGEVENT_ENCRYPTION_ERROR: 
        return "OTRL_MSGEVENT_ENCRYPTION_ERROR";
       case OTRCallbacks.OTRL_MSGEVENT_CONNECTION_ENDED: 
        return "OTRL_MSGEVENT_CONNECTION_ENDED";
       case OTRCallbacks.OTRL_MSGEVENT_SETUP_ERROR: 
        return "OTRL_MSGEVENT_SETUP_ERROR";
       case OTRCallbacks.OTRL_MSGEVENT_MSG_REFLECTED: 
        return "OTRL_MSGEVENT_MSG_REFLECTED";
       case OTRCallbacks.OTRL_MSGEVENT_MSG_RESENT: 
        return "OTRL_MSGEVENT_MSG_RESENT";
       case OTRCallbacks.OTRL_MSGEVENT_RCVDMSG_NOT_IN_PRIVATE: 
        return "OTRL_MSGEVENT_RCVDMSG_NOT_IN_PRIVATE";
       case OTRCallbacks.OTRL_MSGEVENT_RCVDMSG_UNREADABLE: 
        return "OTRL_MSGEVENT_RCVDMSG_UNREADABLE";
       case OTRCallbacks.OTRL_MSGEVENT_RCVDMSG_MALFORMED: 
        return "OTRL_MSGEVENT_RCVDMSG_MALFORMED";
       case OTRCallbacks.OTRL_MSGEVENT_LOG_HEARTBEAT_RCVD: 
        return "OTRL_MSGEVENT_LOG_HEARTBEAT_RCVD";
       case OTRCallbacks.OTRL_MSGEVENT_LOG_HEARTBEAT_SENT: 
        return "OTRL_MSGEVENT_LOG_HEARTBEAT_SENT";
       case OTRCallbacks.OTRL_MSGEVENT_RCVDMSG_GENERAL_ERR: 
        return "OTRL_MSGEVENT_RCVDMSG_GENERAL_ERR";
       case OTRCallbacks.OTRL_MSGEVENT_RCVDMSG_UNENCRYPTED: 
        return "OTRL_MSGEVENT_RCVDMSG_UNENCRYPTED";
       case OTRCallbacks.OTRL_MSGEVENT_RCVDMSG_UNRECOGNIZED: 
        return "OTRL_MSGEVENT_RCVDMSG_UNRECOGNIZED";
    }
    return "Unknown";
  }
  
  public static String getHumanSMPEvent(int event) {
    switch(event) {
      case OTRCallbacks.OTRL_SMPEVENT_NONE:
        return "OTRL_SMPEVENT_NONE";
       case OTRCallbacks.OTRL_SMPEVENT_ERROR:
        return "OTRL_SMPEVENT_ERROR";
       case OTRCallbacks.OTRL_SMPEVENT_ABORT:
        return "OTRL_SMPEVENT_ABORT";
       case OTRCallbacks.OTRL_SMPEVENT_CHEATED:
        return "OTRL_SMPEVENT_CHEATED";
       case OTRCallbacks.OTRL_SMPEVENT_ASK_FOR_ANSWER:
        return "OTRL_SMPEVENT_ASK_FOR_ANSWER";
       case OTRCallbacks.OTRL_SMPEVENT_ASK_FOR_SECRET:
        return "OTRL_SMPEVENT_ASK_FOR_SECRET";
       case OTRCallbacks.OTRL_SMPEVENT_IN_PROGRESS:
        return "OTRL_SMPEVENT_IN_PROGRESS";
       case OTRCallbacks.OTRL_SMPEVENT_SUCCESS:
        return "OTRL_SMPEVENT_SUCCESS";
       case OTRCallbacks.OTRL_SMPEVENT_FAILURE:
        return "OTRL_SMPEVENT_FAILURE";      
    }
    return "Unknown";
  }
  
  public static String encode(byte[] bytes) {
    return Base64.encodeBytes(bytes);
  }
  
  public static String getOwnerJID(SharedPreferences preferences) {
    return preferences.getString(BeemApplication.ACCOUNT_USERNAME_KEY, "");
  }
  
  public static boolean isOTRDisabled(SharedPreferences preferences) {
    return preferences.getBoolean("settings_key_disable_otr", false);
  }
   
  public static String getJIDWithoutResource(String jid) {
    if (jid != null && jid.length() > 0 && jid.lastIndexOf("/") > 1) 
      return jid.substring(0, jid.lastIndexOf("/"));
    return jid;
  }
  
}
