/**
 *  This file is part of GogoDroid.
 *  http://code.google.com/p/gogodroid
 *
 *  GogoDroid is open source software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  GogoDroid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with GogoDroid.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  @author أحمد المحمودي (Ahmed El-Mahmoudy) <aelmahmoudy@sabily.org>
 */
package com.googlecode.gogodroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

public class ConnectivityStateReceiver extends BroadcastReceiver {

  private static final String LOGTAG = ConnectivityStateReceiver.class.getName();

  @Override
  public void onReceive(final Context context, final Intent intent) {
    Log.d(LOGTAG, String.format("Received Broadcast %s", intent));
    if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) return;
    context.startService(GogoService.createIntent(context));
  }
}