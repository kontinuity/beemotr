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

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;

import android.os.RemoteException;

import com.beem.project.beem.service.aidl.IChatManager;
import com.beem.project.beem.service.aidl.IPrivacyListManager;
import com.beem.project.beem.service.aidl.IRoster;
import com.beem.project.beem.service.aidl.IXmppConnection;
import com.beem.project.beem.service.aidl.IXmppFacade;
import com.beem.project.beem.utils.PresenceType;

/**
 * This class is a facade for the Beem Service.
 * 
 * @author darisk
 */
public class XmppFacade extends IXmppFacade.Stub {

  private final XmppConnectionAdapter mConnexion;

  /**
   * Constructor for XMPPFacade.
   * 
   * @param connection the connection use by the facade
   */
  public XmppFacade(final XmppConnectionAdapter connection) {
    this.mConnexion = connection;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void changeStatus(int status, String msg) {
    mConnexion.changeStatus(status, msg);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void connectAsync() throws RemoteException {
    mConnexion.connectAsync();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void connectSync() throws RemoteException {
    mConnexion.connectSync();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IXmppConnection createConnection() throws RemoteException {
    return mConnexion;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void disconnect() throws RemoteException {
    mConnexion.disconnect();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IChatManager getChatManager() throws RemoteException {
    return mConnexion.getChatManager();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IRoster getRoster() throws RemoteException {
    return mConnexion.getRoster();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IPrivacyListManager getPrivacyListManager() {
    return mConnexion.getPrivacyListManager();
  }

  @Override
  public void sendPresencePacket(PresenceAdapter presence) throws RemoteException {
    Presence presence2 = new Presence(PresenceType.getPresenceTypeFrom(presence.getType()));
    presence2.setTo(presence.getTo());
    mConnexion.getAdaptee().sendPacket(presence2);
  }

  /*
   * (non-Javadoc)
   * @see com.beem.project.beem.service.aidl.IXmppFacade#call(java.lang.String)
   */
  @Override
  public void call(String jid) throws RemoteException {
  }

  /*
   * (non-Javadoc)
   * @see
   * com.beem.project.beem.service.aidl.IXmppFacade#getVcardAvatar(java.lang
   * .String)
   */
  @Override
  public byte[] getVcardAvatar(String jid) throws RemoteException {
    VCard vcard = new VCard();

    try {
      vcard.load(mConnexion.getAdaptee(), jid);
      return vcard.getAvatar();
    } catch (XMPPException e) {
      e.printStackTrace();
    }
    return null;
  }
}
