package com.beem.project.beem.utils;

import android.util.Log;

public class L {
  
  public static final String TAG = "OTRBeemChat";
  
  public static void d(String from, Object... params) {
    StringBuilder sb = new StringBuilder("[" + from + "] [");
    for (Object param : params) {
      sb.append('{').append(param).append('}');
    }
    sb.append(']');
    Log.d(TAG, sb.toString());
  }
}
