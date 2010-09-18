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

package com.beem.project.beem.service;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.PrivacyListManager;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.ChatStateManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.beem.project.beem.BeemApplication;
import com.beem.project.beem.BeemService;
import com.beem.project.beem.R;
import com.beem.project.beem.service.aidl.IBeemConnectionListener;
import com.beem.project.beem.service.aidl.IChatManager;
import com.beem.project.beem.service.aidl.IRoster;
import com.beem.project.beem.service.aidl.IXmppConnection;
import com.beem.project.beem.ui.ChangeStatus;
import com.beem.project.beem.ui.Subscription;
import com.beem.project.beem.utils.BeemBroadcastReceiver;
import com.beem.project.beem.utils.Status;

/**
 * This class implements an adapter for XMPPConnection.
 * 
 * @author darisk
 */
public class XmppConnectionAdapter extends IXmppConnection.Stub {

  /**
   * Beem connection closed Intent name.
   */

  private static final int SMACK_PRIORITY_MIN = -128;
  private static final int SMACK_PRIORITY_MAX = 128;
  private static final String TAG = "XMPPConnectionAdapter";
  private final XMPPConnection mAdaptee;
  private IChatManager mChatManager;
  private final String mLogin;
  private final String mPassword;
  private String mResource;
  private String mErrorMsg;
  private RosterAdapter mRoster;
  private int mPreviousPriority;
  private int mPreviousMode;
  private String mPreviousStatus;
  private PrivacyListManagerAdapter mPrivacyListManager;
  private ChatStateManager mChatStateManager;
  private final BeemService mService;
  private BeemApplication mApplication;
  private final RemoteCallbackList<IBeemConnectionListener> mRemoteConnListeners = new RemoteCallbackList<IBeemConnectionListener>();
  private final SubscribePacketListener mSubscribePacketListener = new SubscribePacketListener();

  private final ConnexionListenerAdapter mConListener = new ConnexionListenerAdapter();

  /**
   * Constructor.
   * 
   * @param config Configuration to use in order to connect
   * @param login login to use on connect
   * @param password password to use on connect
   * @param service the background service associated with the connection.
   */
  public XmppConnectionAdapter(final ConnectionConfiguration config, final String login,
      final String password, final BeemService service) {
    this(new XMPPConnection(config), login, password, service);
  }

  /**
   * Constructor.
   * 
   * @param serviceName name of the service to connect to
   * @param login login to use on connect
   * @param password password to use on connect
   * @param service the background service associated with the connection.
   */
  public XmppConnectionAdapter(final String serviceName, final String login, final String password,
      final BeemService service) {
    this(new XMPPConnection(serviceName), login, password, service);
  }

  /**
   * Constructor.
   * 
   * @param con The connection to adapt
   * @param login The login to use
   * @param password The password to use
   * @param service the background service associated with the connection.
   */
  public XmppConnectionAdapter(final XMPPConnection con, final String login, final String password,
      final BeemService service) {
    mAdaptee = con;
    PrivacyListManager.getInstanceFor(mAdaptee);
    mLogin = login;
    mPassword = password;
    mService = service;
    Context ctx = mService.getApplicationContext();
    if (ctx instanceof BeemApplication) {
      mApplication = (BeemApplication)ctx;
    }
    SharedPreferences pref = mService.getServicePreference();
    try {
      mPreviousPriority = Integer.parseInt(pref.getString("settings_key_priority", "0"));
      mResource = pref.getString("settings_key_resource", "BEEM");
    } catch (NumberFormatException ex) {
      mPreviousPriority = 0;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addConnectionListener(IBeemConnectionListener listen) throws RemoteException {
    if (listen != null)
      mRemoteConnListeners.register(listen);
  }

  @Override
  public boolean connect() throws RemoteException {
    if (mAdaptee.isConnected())
      return true;
    else {
      try {
        mAdaptee.connect();
        mAdaptee.addConnectionListener(mConListener);
        return true;
      } catch (XMPPException e) {
        Log.e(TAG, "Error while connecting", e);
        try {
          // TODO NIKITA DOES SOME SHIT !!! Fix this monstruosity
          String str = mService.getResources().getString(
              mService.getResources().getIdentifier(
                  e.getXMPPError().getCondition().replace("-", "_"), "string",
                  "com.beem.project.beem"));
          mErrorMsg = str;
        } catch (NullPointerException e2) {
          if (!"".equals(e.getMessage()))
            mErrorMsg = e.getMessage();
          else
            mErrorMsg = e.toString();
        }
      }
      return false;
    }
  }

  @Override
  public boolean login() throws RemoteException {
    if (mAdaptee.isAuthenticated())
      return true;
    if (!mAdaptee.isConnected())
      return false;
    try {

      this.initFeatures(); // pour declarer les features xmpp qu'on
      // supporte

      PacketFilter filter = new PacketFilter() {

        @Override
        public boolean accept(Packet packet) {
          if (packet instanceof Presence) {
            Presence pres = (Presence)packet;
            if (pres.getType() == Presence.Type.subscribe)
              return true;
          }
          return false;
        }
      };

      mAdaptee.addPacketListener(mSubscribePacketListener, filter);

      mAdaptee.login(mLogin, mPassword, mResource);
      mChatManager = new BeemChatManager(mAdaptee.getChatManager(), mService);
      mPrivacyListManager = new PrivacyListManagerAdapter(
          PrivacyListManager.getInstanceFor(mAdaptee));
      mService.resetStatus();
      mService.initJingle(mAdaptee);

      mApplication.setConnected(true);
      changeStatus(Status.CONTACT_STATUS_AVAILABLE,
          mService.getServicePreference().getString("status_text", ""));
      return true;
    } catch (XMPPException e) {
      Log.e(TAG, "Error while connecting", e);
      mErrorMsg = mService.getString(R.string.error_login_authentication);
      return false;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void connectAsync() throws RemoteException {
    if (mAdaptee.isConnected() || mAdaptee.isAuthenticated())
      return;
    Thread t = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          connectSync();
        } catch (RemoteException e) {
          Log.e(TAG, "Error while connecting asynchronously", e);
        }
      }
    });
    t.start();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean connectSync() throws RemoteException {
    if (connect())
      return login();
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void changeStatusAndPriority(int status, String msg, int priority) {
    Presence pres = new Presence(Presence.Type.available);
    String m;
    if (msg != null)
      m = msg;
    else
      m = mPreviousStatus;
    pres.setStatus(m);
    mPreviousStatus = m;
    Presence.Mode mode = Status.getPresenceModeFromStatus(status);
    if (mode != null) {
      pres.setMode(mode);
      mPreviousMode = status;
    } else {
      pres.setMode(Status.getPresenceModeFromStatus(mPreviousMode));
    }
    int p = priority;
    if (priority < SMACK_PRIORITY_MIN)
      p = SMACK_PRIORITY_MIN;
    if (priority > SMACK_PRIORITY_MAX)
      p = SMACK_PRIORITY_MAX;
    mPreviousPriority = p;
    pres.setPriority(p);
    mAdaptee.sendPacket(pres);
    updateNotification(m);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void changeStatus(int status, String msg) {
    changeStatusAndPriority(status, msg, mPreviousPriority);
  }

  /**
   * get the previous status.
   * 
   * @return previous status.
   */
  public String getPreviousStatus() {
    return mPreviousStatus;
  }

  /**
   * get the previous mode.
   * 
   * @return previous mode.
   */
  public int getPreviousMode() {
    return mPreviousMode;
  }

  /**
   * Update the notification for the Beem status.
   * 
   * @param text the text to display.
   */
  private void updateNotification(String text) {
    Notification mStatusNotification;
    mStatusNotification = new Notification(com.beem.project.beem.R.drawable.beem_status_icon, text,
        System.currentTimeMillis());
    mStatusNotification.defaults = Notification.DEFAULT_LIGHTS;
    mStatusNotification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

    mStatusNotification.setLatestEventInfo(mService, "Beem Status", text,
        PendingIntent.getActivity(mService, 0, new Intent(mService, ChangeStatus.class), 0));
    // bypass the preferences for notification
    mService.getNotificationManager().notify(BeemService.NOTIFICATION_STATUS_ID,
        mStatusNotification);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean disconnect() {
    if (mAdaptee != null && mAdaptee.isConnected())
      mAdaptee.disconnect();
    return true;
  }

  /**
   * Get the Smack XmppConnection.
   * 
   * @return Smack XmppConnection
   */
  public XMPPConnection getAdaptee() {
    return mAdaptee;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IChatManager getChatManager() throws RemoteException {
    return mChatManager;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IRoster getRoster() throws RemoteException {
    if (mRoster != null)
      return mRoster;
    Roster adap = mAdaptee.getRoster();
    if (adap == null)
      return null;
    mRoster = new RosterAdapter(adap, mService);
    return mRoster;
  }

  /**
   * enregistre les features dispo dans notre version Liste de features que
   * Telepathy supporte.
   */
  private void initFeatures() {
    ServiceDiscoveryManager.setIdentityName("Beem");
    ServiceDiscoveryManager.setIdentityType("phone");
    ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(mAdaptee);
    if (sdm == null)
      sdm = new ServiceDiscoveryManager(mAdaptee);
    sdm.addFeature("http://jabber.org/protocol/disco#info");
    sdm.addFeature("jabber:iq:privacy");
    sdm.addFeature("http://jabber.org/protocol/caps");
    mChatStateManager = ChatStateManager.getInstance(mAdaptee);
    BeemCapsManager caps = new BeemCapsManager(sdm, mAdaptee, mService);
    caps.setNode("http://www.beem-project.com");
  }

  /**
   * Returns true if currently authenticated by successfully calling the login
   * method.
   * 
   * @return true when successfully authenticated
   */
  public boolean isAuthentificated() {
    return mAdaptee.isAuthenticated();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeConnectionListener(IBeemConnectionListener listen) throws RemoteException {
    if (listen != null)
      mRemoteConnListeners.unregister(listen);
  }

  /**
   * PrivacyListManagerAdapter mutator.
   * 
   * @param privacyListManager the privacy list manager
   */
  public void setPrivacyListManager(PrivacyListManagerAdapter privacyListManager) {
    this.mPrivacyListManager = privacyListManager;
  }

  /**
   * PrivacyListManagerAdapter accessor.
   * 
   * @return the mPrivacyList
   */
  public PrivacyListManagerAdapter getPrivacyListManager() {
    return mPrivacyListManager;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getErrorMessage() {
    return mErrorMsg;
  }

  /**
   * Listener for XMPP connection events. It will calls the remote listeners for
   * connection events.
   * 
   * @author darisk
   */
  private class ConnexionListenerAdapter implements ConnectionListener {

    /**
     * Defaut constructor.
     */
    public ConnexionListenerAdapter() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectionClosed() {
      Log.d(TAG, "closing connection");
      mRoster = null;
      Intent intent = new Intent(BeemBroadcastReceiver.BEEM_CONNECTION_CLOSED);
      intent.putExtra("message", mService.getString(R.string.BeemBroadcastReceiverDisconnect));
      intent.putExtra("normally", true);
      mService.sendBroadcast(intent);
      mService.stopSelf();
      mApplication.setConnected(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectionClosedOnError(Exception exception) {
      Log.d(TAG, "connectionClosedOnError");
      mRoster = null;
      Intent intent = new Intent(BeemBroadcastReceiver.BEEM_CONNECTION_CLOSED);
      intent.putExtra("message", exception.getMessage());
      mService.sendBroadcast(intent);
      mService.stopSelf();
      mApplication.setConnected(false);
    }

    /**
     * Connection failed callback.
     * 
     * @param errorMsg smack failure message
     */
    public void connectionFailed(String errorMsg) {
      Log.d(TAG, "Connection Failed");
      final int n = mRemoteConnListeners.beginBroadcast();

      for (int i = 0; i < n; i++) {
        IBeemConnectionListener listener = mRemoteConnListeners.getBroadcastItem(i);
        try {
          if (listener != null)
            listener.connectionFailed(errorMsg);
        } catch (RemoteException e) {
          // The RemoteCallbackList will take care of removing the
          // dead listeners.
          Log.w(TAG, "Error while triggering remote connection listeners", e);
        }
      }
      mRemoteConnListeners.finishBroadcast();
      mService.stopSelf();
      mApplication.setConnected(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reconnectingIn(int arg0) {
      Log.d(TAG, "reconnectingIn");
      final int n = mRemoteConnListeners.beginBroadcast();

      for (int i = 0; i < n; i++) {
        IBeemConnectionListener listener = mRemoteConnListeners.getBroadcastItem(i);
        try {
          if (listener != null)
            listener.reconnectingIn(arg0);
        } catch (RemoteException e) {
          // The RemoteCallbackList will take care of removing the
          // dead listeners.
          Log.w(TAG, "Error while triggering remote connection listeners", e);
        }
      }
      mRemoteConnListeners.finishBroadcast();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reconnectionFailed(Exception arg0) {
      Log.d(TAG, "reconnectionFailed");
      final int r = mRemoteConnListeners.beginBroadcast();

      for (int i = 0; i < r; i++) {
        IBeemConnectionListener listener = mRemoteConnListeners.getBroadcastItem(i);
        try {
          if (listener != null)
            listener.reconnectionFailed();
        } catch (RemoteException e) {
          // The RemoteCallbackList will take care of removing the
          // dead listeners.
          Log.w(TAG, "Error while triggering remote connection listeners", e);
        }
      }
      mRemoteConnListeners.finishBroadcast();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reconnectionSuccessful() {
      Log.d(TAG, "reconnectionSuccessful");
      mApplication.setConnected(true);
      PacketFilter filter = new PacketFilter() {

        @Override
        public boolean accept(Packet packet) {
          if (packet instanceof Presence) {
            Presence pres = (Presence)packet;
            if (pres.getType() == Presence.Type.subscribe)
              return true;
          }
          return false;
        }
      };

      mAdaptee.addPacketListener(new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
          String from = packet.getFrom();
          Notification notif = new Notification(android.R.drawable.stat_notify_more, mService
              .getString(R.string.AcceptContactRequest, from), System.currentTimeMillis());
          notif.flags = Notification.FLAG_AUTO_CANCEL;
          Intent intent = new Intent(mService, Subscription.class);
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("from", from);
          notif.setLatestEventInfo(mService, from,
              mService.getString(R.string.AcceptContactRequestFrom, from),
              PendingIntent.getActivity(mService, 0, intent, PendingIntent.FLAG_ONE_SHOT));
          int id = packet.hashCode();
          mService.sendNotification(id, notif);
        }
      }, filter);

      final int n = mRemoteConnListeners.beginBroadcast();

      for (int i = 0; i < n; i++) {
        IBeemConnectionListener listener = mRemoteConnListeners.getBroadcastItem(i);
        try {
          if (listener != null)
            listener.reconnectionSuccessful();
        } catch (RemoteException e) {
          // The RemoteCallbackList will take care of removing the
          // dead listeners.
          Log.w(TAG, "Error while triggering remote connection listeners", e);
        }
      }
      mRemoteConnListeners.finishBroadcast();
    }
  }

  /**
   * This PacketListener will set a notification when you got a subscribtion
   * request.
   * 
   * @author Da Risk <da_risk@elyzion.net>
   */
  private class SubscribePacketListener implements PacketListener {

    /**
     * Constructor.
     */
    public SubscribePacketListener() {
    }

    @Override
    public void processPacket(Packet packet) {
      if (!(packet instanceof Presence))
        return;
      Presence p = (Presence)packet;
      if (p.getType() != Presence.Type.subscribe)
        return;
      String from = p.getFrom();
      Notification notification = new Notification(android.R.drawable.stat_notify_more,
          mService.getString(R.string.AcceptContactRequest, from), System.currentTimeMillis());
      notification.flags = Notification.FLAG_AUTO_CANCEL;
      Intent intent = new Intent(mService, Subscription.class);
      intent.putExtra("from", from);
      notification.setLatestEventInfo(mService, from,
          mService.getString(R.string.AcceptContactRequestFrom, from),
          PendingIntent.getActivity(mService, 0, intent, PendingIntent.FLAG_ONE_SHOT));
      int id = p.hashCode();
      mService.sendNotification(id, notification);
    }
  }

}
