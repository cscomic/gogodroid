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
 *  @author Mariotaku Lee (mariotaku) <mariotaku.lee@gmail.com>
 */
package com.googlecode.gogodroid;

import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.EditText;
import android.widget.TextView;

import com.googlecode.gogodroid.R;

public class GogoDroid extends Activity {

  RadioButton StatusRunning;
  Button btnStartStop;
  TextView gogocConfig;
  TextView currentIP;
  String linkstatus;
  private ServiceConnection mConnection;
  private GogoServiceIface mGogoService;
  private boolean mBound;
  private BroadcastReceiver refreshReceiver;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    refreshReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        Log.d(Constants.LOG_TAG, "Received refreshUI request");
        refreshUI();
      }
    };
  
  }

  @Override
  public void onContentChanged() {
    StatusRunning = (RadioButton) findViewById(R.id.Running);
    gogocConfig = (TextView) findViewById(R.id.GogocConf);
    currentIP = (TextView) findViewById(R.id.Address);
    btnStartStop = (Button) findViewById(R.id.ButtonStartStop);
    btnStartStop.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        startStopGogoc();
      }
    });

    registerReceiver(refreshReceiver, new IntentFilter(Constants.RefreshUIAction));
    refreshUI();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mConnection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName className, IBinder binder) {
        mGogoService = GogoServiceIface.Stub.asInterface(binder);
        mBound = true;
        setContentView(R.layout.main);
      }

      @Override
      public void onServiceDisconnected(ComponentName className) {
        mGogoService = null;
        mBound = false;
      }
    };
    bindService(GogoService.createIntent(this), mConnection, Context.BIND_AUTO_CREATE);
  }

  @Override
  protected void onStop() {
    super.onStop();
    unregisterReceiver(refreshReceiver);
    if (mBound) {
      unbindService(mConnection);
      mBound = false;
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    if(!mBound) {
      return(false);
    }
    MenuItem startStopItem = menu.findItem(R.id.start_stop);
    try {
      if( mGogoService.statusGogoc()) {
        startStopItem.setTitle(R.string.btn_stop);
      }
      else {
        startStopItem.setTitle(R.string.btn_start);
      }
    }
    catch (RemoteException e) {
      Log.e(Constants.LOG_TAG, "", e);
    }
    return true;
  }
    
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
      case R.id.action_preferences:
        startActivity(new Intent(this, GogoPreferenceActivity.class));
        return true;
      case R.id.action_exit:
        try {
          if ( mGogoService.statusGogoc()) {
            mGogoService.stopGogoc(true);
          }
        }
        catch (RemoteException e) {
          Log.e(Constants.LOG_TAG, "", e);
        }
        finish();
        return true;
      case R.id.set_dns:
        setDNS();
        //mGogoService.showToast(R.string.dns_changed);
        return true;
      case R.id.start_stop:
        startStopGogoc();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }

  }


  @SuppressWarnings("static-access")
  public void setDNS(){
      
    Thread thread = new Thread() {
      @Override
      public void run() {
          Utils.runSuCommand("setprop net.dns1 " + Constants.DNS1 );
      }
    };
    thread.start();
    // sleep to give some time to statusGogoc to detect process
    try{
      Thread.currentThread().sleep(1000);//sleep for 1000 ms
    }
    catch(Exception e){
      e.printStackTrace();
    }

  }

  private void startStopGogoc() {
    try{
      if( !mGogoService.statusGogoc()) {
        // save conf in file
        mGogoService.startGogoc();
      }
      else {
        mGogoService.stopGogoc(true);
      }
      refreshUI();
    }
    catch (RemoteException e) {
      Log.e(Constants.LOG_TAG, "", e);
    }
  }

  private void refreshUI() {
    // Update start/stop button label
    try {
      if( mGogoService.statusGogoc()) {
        btnStartStop.setText(R.string.btn_stop);
      }
      else {
        btnStartStop.setText(R.string.btn_start);
      }
    }
    catch (RemoteException e) {
      Log.e(Constants.LOG_TAG, "", e);
    }

    // load configuration in gogocConfig
    try {
      gogocConfig.setText( mGogoService.loadConf().toString() );
    }
    catch (RemoteException e) {
      Log.e(Constants.LOG_TAG, "", e);
    }
    
    // check gogodroid status
    try {
      linkstatus = mGogoService.statusConnection(false);
      showIndicator(linkstatus);
    }
    catch (RemoteException e) {
      Log.e(Constants.LOG_TAG, "", e);
    }
  }

  private void showIndicator(String status) {
    if (status.equals("not_available")){
      currentIP.setText( R.string.not_available );
      StatusRunning.setPressed(false);
      StatusRunning.setChecked(false);
    }
    if (status.equals("connecting")){
      currentIP.setText(R.string.gogoc_connecting);
      StatusRunning.setPressed(true);
      StatusRunning.setChecked(false);
    }
    if (status.startsWith("established")){
      currentIP.setText(status.substring(12, status.length()));
      StatusRunning.setPressed(false);
      StatusRunning.setChecked(true);
    }
    if (status.equals("error")){
      currentIP.setText(R.string.status_error);
      StatusRunning.setPressed(false);
      StatusRunning.setChecked(false);
    }
  }

}
