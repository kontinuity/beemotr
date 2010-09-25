
package com.beem.project.beem.service;

import ca.uwaterloo.crysp.otr.TLV;
import ca.uwaterloo.crysp.otr.UserState;
import ca.uwaterloo.crysp.otr.iface.OTRInterface;
import ca.uwaterloo.crysp.otr.iface.OTRTLV;

public class OTRManager {

  private static OTRManager instance;
  private OTRInterface otr;
  private OTRTLV[] tlvs;

  public static OTRManager getInstance() {
    if (instance == null) {
      instance = new OTRManager();
    }
    return instance;
  }

  private OTRManager() {
    this.otr = new UserState(new ca.uwaterloo.crysp.otr.crypt.jca.JCAProvider());
    tlvs = new OTRTLV[1];
    getTlvs()[0] = new TLV(9, "TestTLV".getBytes());
  }

  public OTRInterface getInterface() {
    return otr;
  }

  public OTRTLV[] getTlvs() {
    return tlvs;
  }

}
