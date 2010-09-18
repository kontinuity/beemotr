package com.beem.project.beem.service;

import ca.uwaterloo.crysp.otr.UserState;
import ca.uwaterloo.crysp.otr.crypt.jca.JCAProvider;

public class OTRManager {
  
  private static OTRManager instance;
  private UserState userState;
  
  public static OTRManager getInstance() {
    if (instance == null) {
      instance = new OTRManager();
    }
    return instance;
  }
  
  private OTRManager() {
    this.userState = new UserState(new JCAProvider());
  }
  
  public UserState getUserState() {
    return userState;
  }
  
}
