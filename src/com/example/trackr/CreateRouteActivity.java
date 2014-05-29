package com.example.trackr;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class CreateRouteActivity extends FragmentActivity {
    private ViewPager mViewPager;
    private RouteAdapter mFragAdapter;



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager);

        mFragAdapter = new RouteAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mFragAdapter);
    }

    public static class RouteAdapter extends FragmentPagerAdapter {
        public RouteAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new RouteSearchActivity();
                case 1:
                    return new GoogleMapActivity();
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