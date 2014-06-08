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
import android.util.Log;
import android.view.WindowManager;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class RouteInfoActivity extends FragmentActivity {

    public static String url = null;
    public int dataIndex;
    private TrackerService mService;
    private boolean serviceBound = false;
    private boolean background = false;
    public data routeData = null;
    static String cRoute = null;
    static String tRoute = null;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        TrackerAdapter mFragAdapter = new TrackerAdapter(getSupportFragmentManager());
        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        dataIndex = getIntent().getIntExtra("dataIndex", dataIndex);
        routeData = HomeActivity.getTrackedRoute(dataIndex);
        cRoute = routeData.route;
        tRoute = routeData.trackedRoute;
        if(routeData == null) Log.i("NULL", "NULL");
        mViewPager.setAdapter(mFragAdapter);
        
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    public static class TrackerAdapter extends FragmentPagerAdapter {
        public TrackerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return RouteViewActivity.newInstance("View " + position);
                case 1:
                    return GoogleMapActivity.newInstance(false, cRoute, tRoute);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }


}