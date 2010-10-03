package com.beem.project.beem.utils;

import static com.beem.project.beem.utils.CommonUtils.d;
import static com.beem.project.beem.utils.CommonUtils.e;
import static com.beem.project.beem.utils.CommonUtils.getJIDWithoutResource;
import android.os.AsyncTask;
import ca.uwaterloo.crysp.otr.Account;
import ca.uwaterloo.crysp.otr.OTRException;
import ca.uwaterloo.crysp.otr.UserState;

import com.beem.project.beem.service.OTRManager;

public class CryptWarmer extends AsyncTask {

  @Override
  protected Object doInBackground(Object... params) {
    d("CryptWarmer.doInBackground", "Starting to warm crypt", getJIDWithoutResource(params[0].toString()));
    long start = System.currentTimeMillis();
    Account account = new Account(getJIDWithoutResource(params[0].toString()), "xmpp");
    try {
      ((UserState) OTRManager.getInstance().getInterface()).getPrivKey(account, true);
      d("CryptWarmer.doInBackground", "Finished warming crypt in " + (System.currentTimeMillis() - start) / 1000 + " seconds");
    } catch (OTRException ex) {
      e("CryptWarmer.doInBackground", ex, "Failed to warm crypt. Wasted " + (System.currentTimeMillis() - start) / 1000 + " seconds");
    }
    return null;
  }

}
