package com.example.trackr;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import android.widget.TextView;
import com.example.trackr.TrackerService.MyBinder;

public class test extends Activity {
    private TrackerService mService;
    private Handler mHandler;
    private static final String LOG_TAG = "MyActivity";
    private boolean serviceBound = false;
    private boolean background = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        // Create a new handler that update the display every 100ms
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //updateDisplay();
                mHandler.sendEmptyMessageDelayed(0, 100);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start Service and restart handler if return from background;
       // startService();
        mHandler.sendEmptyMessageDelayed(0, 100);
//        Log.i(LOG_TAG, "Resume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop handler from updating the screen
        if(serviceBound) {
            mHandler.removeMessages(0);
            unbindService(mServiceConnection);
            background = true;
//            Log.i(LOG_TAG, "Background");
        }
    }

    // Service connection
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyBinder mBinder = (MyBinder) iBinder;
            mService = mBinder.getServicesInstance();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceBound = false;
        }
    };

    // Call Start and Stop function
    public void onClickStart(View v) {
        if(!serviceBound) {
            startService();
        } else {
            stopService();
        }
    }

    // Start service, bind service and start the handler
    public void startService() {
        startService(new Intent(getBaseContext(), TrackerService.class));
        bindMyService();
        serviceBound = true;
        mHandler.sendEmptyMessageDelayed(0, 100);
    }

    // Stop service, unbind the service and stop the handler
    public void stopService() {
        unbindService(mServiceConnection);
        stopService(new Intent(getBaseContext(), TrackerService.class));
        serviceBound = false;
        mHandler.removeMessages(0);
    }

    // Update display
    public void updateDisplay() {

    }

    // reset max acceleration and time
    public void onClickReset(View v) {
        //mService.resetData();
        updateDisplay();
    }

    // Bind service
    private void bindMyService() {
        Intent intent = new Intent(this, TrackerService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }
}