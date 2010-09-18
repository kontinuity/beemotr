/*
    BEEM is a videoconference application on the Android Platform.

    Copyright (C) 2009 by Frederic-Charles Barthelery,
                          Jean-Manuel Da Silva,
                          Nikita Kozlov,
                          Philippe Lago,
                          Jean Baptiste Vergely,
                          Vincent Veronis.

    This file is part of BEEM.

    BEEM is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BEEM is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with BEEM.  If not, see <http://www.gnu.org/licenses/>.

    Please send bug reports with examples or suggestions to
    contact@beem-project.com or http://dev.beem-project.com/

    Epitech, hereby disclaims all copyright interest in the program "Beem"
    written by Frederic-Charles Barthelery,
               Jean-Manuel Da Silva,
               Nikita Kozlov,
               Philippe Lago,
               Jean Baptiste Vergely,
               Vincent Veronis.

    Nicolas Sadirac, November 26, 2009
    President of Epitech.

    Flavien Astraud, November 26, 2009
    Head of the EIP Laboratory.

 */

package com.beem.project.beem.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.beem.project.beem.BeemApplication;
import com.beem.project.beem.BeemService;
import com.beem.project.beem.R;
import com.beem.project.beem.service.aidl.IXmppFacade;
import com.beem.project.beem.utils.BeemBroadcastReceiver;
import com.beem.project.beem.utils.BeemConnectivity;
import com.beem.project.beem.utils.Status;

/**
 * This Activity is used to change the status.
 * 
 * @author nikita
 */
public class ChangeStatus extends Activity {

  private static final Intent SERVICE_INTENT = new Intent();
  static {
    SERVICE_INTENT.setComponent(new ComponentName("com.beem.project.beem",
        "com.beem.project.beem.BeemService"));
  }

  private static final int AVAILABLE_FOR_CHAT_IDX = 0;
  private static final int AVAILABLE_IDX = 1;
  private static final int BUSY_IDX = 2;
  private static final int AWAY_IDX = 3;
  private static final int UNAVAILABLE_IDX = 4;
  private static final int DISCONNECTED_IDX = 5;

  private EditText mStatusMessageEditText;
  private Toast mToast;
  private Button mOk;
  private Button mClear;
  private Button mContact;
  private Spinner mSpinner;

  private SharedPreferences mSettings;
  private ArrayAdapter<CharSequence> mAdapter;
  private IXmppFacade mXmppFacade;
  private final ServiceConnection mServConn = new BeemServiceConnection();
  private final OnClickListener mOnClickOk = new MyOnClickListener();
  private final BeemBroadcastReceiver mReceiver = new BeemBroadcastReceiver();

  /**
   * Constructor.
   */
  public ChangeStatus() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.changestatus);

    mOk = (Button)findViewById(R.id.ChangeStatusOk);
    mOk.setOnClickListener(mOnClickOk);

    mClear = (Button)findViewById(R.id.ChangeStatusClear);
    mClear.setOnClickListener(mOnClickOk);

    mContact = (Button)findViewById(R.id.OpenContactList);
    mContact.setOnClickListener(mOnClickOk);

    mSettings = PreferenceManager.getDefaultSharedPreferences(this);
    mStatusMessageEditText = (EditText)findViewById(R.id.ChangeStatusMessage);
    mStatusMessageEditText.setText(mSettings.getString(BeemApplication.STATUS_TEXT_KEY, ""));

    mSpinner = (Spinner)findViewById(R.id.ChangeStatusSpinner);
    mAdapter = ArrayAdapter.createFromResource(this, R.array.status_types,
        android.R.layout.simple_spinner_item);
    mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mSpinner.setAdapter(mAdapter);

    mToast = Toast.makeText(this, R.string.ChangeStatusOk, Toast.LENGTH_LONG);
    mSpinner.setSelection(getPreferenceStatusIndex());

    this.registerReceiver(mReceiver, new IntentFilter(BeemBroadcastReceiver.BEEM_CONNECTION_CLOSED));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onResume() {
    super.onResume();
    if (!BeemConnectivity.isConnected(getApplicationContext())) {
      Intent i = new Intent(this, Login.class);
      startActivity(i);
      finish();
    }
    bindService(new Intent(this, BeemService.class), mServConn, BIND_AUTO_CREATE);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onPause() {
    super.onPause();
    Log.d("TAG", "pause");
    unbindService(mServConn);
  }

  /*
   * (non-Javadoc)
   * @see android.app.Activity#onDestroy()
   */
  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.unregisterReceiver(mReceiver);
  }

  /**
   * Return the status index from status the settings.
   * 
   * @return the status index from status the settings.
   */
  private int getPreferenceStatusIndex() {
    return mSettings.getInt(BeemApplication.STATUS_KEY, AVAILABLE_IDX);
  }

  /**
   * Return the status text from status the settings.
   * 
   * @param id status text id.
   * @return the status text from status the settings.
   */
  private String getPreferenceString(int id) {
    return mSettings.getString(getString(id), "");
  }

  /**
   * convert status text to.
   * 
   * @param item selected item text.
   * @return item position in the array.
   */
  private int getStatusForService(String item) {
    int result;
    switch (mAdapter.getPosition(item)) {
      case ChangeStatus.DISCONNECTED_IDX:
        result = Status.CONTACT_STATUS_DISCONNECT;
        break;
      case ChangeStatus.AVAILABLE_FOR_CHAT_IDX:
        result = Status.CONTACT_STATUS_AVAILABLE_FOR_CHAT;
        break;
      case ChangeStatus.AVAILABLE_IDX:
        result = Status.CONTACT_STATUS_AVAILABLE;
        break;
      case ChangeStatus.AWAY_IDX:
        result = Status.CONTACT_STATUS_AWAY;
        break;
      case ChangeStatus.BUSY_IDX:
        result = Status.CONTACT_STATUS_BUSY;
        break;
      case ChangeStatus.UNAVAILABLE_IDX:
        result = Status.CONTACT_STATUS_UNAVAILABLE;
        break;
      default:
        result = Status.CONTACT_STATUS_AVAILABLE;
        break;
    }
    return result;
  }

  /**
   * connection to service.
   * 
   * @author nikita
   */
  private class BeemServiceConnection implements ServiceConnection {

    /**
     * constructor.
     */
    public BeemServiceConnection() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      mXmppFacade = IXmppFacade.Stub.asInterface(service);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {
      mXmppFacade = null;
    }
  }

  /**
   * User have clicked on ok.
   * 
   * @author nikita
   */
  private class MyOnClickListener implements OnClickListener {

    /**
     * constructor.
     */
    public MyOnClickListener() {
    }

    @Override
    public void onClick(View v) {
      if (v == mOk) {
        String msg = mStatusMessageEditText.getText().toString();
        int status = getStatusForService((String)mSpinner.getSelectedItem());
        Editor edit = mSettings.edit();
        edit.putString(BeemApplication.STATUS_TEXT_KEY, msg);
        if (status == Status.CONTACT_STATUS_DISCONNECT) {
          stopService(new Intent(ChangeStatus.this, BeemService.class));
        } else {
          try {
            mXmppFacade.changeStatus(status, msg.toString());
            edit.putInt(BeemApplication.STATUS_KEY, mSpinner.getSelectedItemPosition());
          } catch (RemoteException e) {
            e.printStackTrace();
          }
          mToast.show();
        }
        edit.commit();
        ChangeStatus.this.finish();
      } else if (v == mClear) {
        mStatusMessageEditText.setText(null);
      } else if (v == mContact) {
        startActivity(new Intent(ChangeStatus.this, ContactList.class));
        ChangeStatus.this.finish();
      }
    }
  }
}
