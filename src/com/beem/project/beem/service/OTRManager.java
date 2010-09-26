
package com.beem.project.beem.service;

import ca.uwaterloo.crysp.otr.UserState;
import ca.uwaterloo.crysp.otr.iface.OTRInterface;

public class OTRManager {

  private static OTRManager instance;
  private OTRInterface otr;

  public static OTRManager getInstance() {
    if (instance == null) {
      instance = new OTRManager();
    }
    return instance;
  }

  private OTRManager() {
    this.otr = new UserState(new ca.uwaterloo.crysp.otr.crypt.jca.JCAProvider());
  }

  public OTRInterface getInterface() {
    return otr;
  }

}
