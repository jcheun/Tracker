package com.example.trackr;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.WindowManager;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class TrackerActivity extends FragmentActivity {

    public static String url = null;

    private TrackerService mService;
    private boolean serviceBound = false;
    private boolean background = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        TrackerAdapter mFragAdapter = new TrackerAdapter(getSupportFragmentManager());
        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);

        url = getIntent().getStringExtra("URL");

        mViewPager.setAdapter(mFragAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(serviceBound) {
            unbindService(mServiceConnection);
            background = true;
        }
    }

    // Service connection
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            TrackerService.MyBinder mBinder = (TrackerService.MyBinder) iBinder;
            mService = mBinder.getServicesInstance();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceBound = false;
        }
    };

    // Call Start and Stop function
    public Boolean StartStop() {
        if(!serviceBound) {
            startService();
        } else {
            stopService();
        }
        return serviceBound;
    }

    // Start service, bind service and start the handler
    public void startService() {
        startService(new Intent(getBaseContext(), TrackerService.class));
        bindMyService();
        serviceBound = true;
    }

    // Stop service, unbind the service and stop the handler
    public void stopService() {
        unbindService(mServiceConnection);
        stopService(new Intent(getBaseContext(), TrackerService.class));
        serviceBound = false;
    }

    // Bind service
    private void bindMyService() {
        Intent intent = new Intent(this, TrackerService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }


    public static class TrackerAdapter extends FragmentPagerAdapter {
        public TrackerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return TrackActivity.newInstance("View " + position);
                case 1:
                    return GoogleMapActivity.newInstance(false, url, null);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public List<LatLng> getCurrentPoints() {
        return mService.getCurrentPoints();
    }

    public double getCurrentSpeed() {
        return mService.getCurrentSpeed();
    }

    public double getCurrentDistance() {
        return mService.getCurrentDistance();
    }

    public float getCurrentBearing() {
        return mService.getCurrentBearing();
    }

    public long getTime() {
        return mService.getTime();
    }

    public List<Double> getSpeeds() {
        return mService.getSpeeds();
    }

    public List<Double> getAltitudes() {
        return mService.getAltitudes();
    }

    public void enableLogging(Boolean enable) {
        mService.enableLogging(enable);
    }

    public boolean isLogging() {
        return mService.isLogging();
    }

    public boolean isRunning() {
        return mService.isRunning();
    }

    public long getFinalTime() {
        return mService.getFinalTime();
    }

    public LatLng getCurrentPosition() {
        return mService.getCurrnetLocation();
    }

//    public long getCurrentTime() {
//        return mService.getCurrentTime();
//    }
}