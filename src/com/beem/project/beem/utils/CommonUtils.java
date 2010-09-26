package com.beem.project.beem.utils;

import org.jivesoftware.smack.util.Base64;

import com.beem.project.beem.BeemApplication;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import ca.uwaterloo.crysp.otr.MsgState;
import ca.uwaterloo.crysp.otr.Proto;

public class CommonUtils {
  
  public static final String TAG = "OTRBeemChat";
  
  public static void d(String from, Object... params) {
    Log.d(TAG, generateLogMessage(from, params));
  }

  public static void e(String from, Throwable e, Object... params) {
    Log.e(TAG, generateLogMessage(from, params), e);
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
        return "MsgState.ST_UNENCRYPTED"; 
      case MsgState.ST_ENCRYPTED:
        return "MsgState.ST_ENCRYPTED";
      case MsgState.ST_FINISHED:
        return "MsgState.ST_FINISHED";
    }
    return "Unknown";
  }
  
  public static String getHumanFragmentState(int state) {
    switch (state) {
      case Proto.FRAGMENT_UNFRAGMENTED:
        return "Proto.FRAGMENT_UNFRAGMENTED";
      case Proto.FRAGMENT_INCOMPLETE:
        return "Proto.FRAGMENT_INCOMPLETE";
      case Proto.FRAGMENT_COMPLETE:
        return "Proto.FRAGMENT_COMPLETE";
    }    
    return "Unknown";
  }
  
  public static String encode(byte[] bytes) {
    return Base64.encodeBytes(bytes);
  }
  
  public static String getOwnerJID(SharedPreferences preferences) {
    return preferences.getString(BeemApplication.ACCOUNT_USERNAME_KEY, "");
  }
  
  public static String getJIDWithoutResource(String jid) {
    if (jid != null && jid.length() > 0 && jid.lastIndexOf("/") > 1) 
      return jid.substring(0, jid.lastIndexOf("/"));
    return jid;
  }
  
}
