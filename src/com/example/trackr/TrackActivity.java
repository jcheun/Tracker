package com.example.trackr;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class TrackActivity extends Fragment {
    private static final String LOG_TAG = "TrackActivity";

    private TextView textTimer;
    private Handler myHandler = new Handler();
    private long startTime = 0L;
    private long timeMilli = 0L;
    private long timeSwap = 0L;
    private long finalTime = 0L;
    private boolean isRunning = false;


    public static TrackActivity newInstance(String title) {
        TrackActivity trackFragment = new TrackActivity();
        Bundle bundle = new Bundle();
        bundle.putString("Title", title);
        trackFragment.setArguments(bundle);
        return trackFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(LOG_TAG, "Created View");
        View view = inflater.inflate(R.layout.activity_timer, container, false);
        TextView textView = (TextView) view.findViewById(R.id.textTimer);
        textView.setText(getArguments().getString("Title"));
        Button btn = (Button) view.findViewById(R.id.Start);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRunning == false) {
                    isRunning = true;
                    startTime = SystemClock.uptimeMillis();
                    myHandler.postDelayed(updateTimerMethod, 0);
                } else {
                    isRunning = false;
                    timeSwap += timeMilli;
                    myHandler.removeCallbacks(updateTimerMethod);
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //myHandler.postDelayed(updateTimerMethod, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        myHandler.removeCallbacks(updateTimerMethod);
    }

    private Runnable updateTimerMethod = new Runnable() {

        public void run() {
//            finalTime = TrackerActivity.getCurrentTime();
//            textTimer = (TextView) getView().findViewById(R.id.textTimer);
//            int seconds = (int) (finalTime / 1000);
//            int minutes = seconds / 60;
//            seconds = seconds % 60;
//            int milliseconds = (int) (finalTime % 1000);
//            textTimer.setText("" + minutes + ":"
//                    + String.format("%02d", seconds) + ":"
//                    + String.format("%02d", milliseconds));
//
            TextView speed = (TextView) getActivity().findViewById(R.id.currentSpeed);
            TextView distance = (TextView) getActivity().findViewById(R.id.distTravel);
            GoogleMapActivity gMapFrag = (GoogleMapActivity) getFragmentManager().findFragmentByTag("android:switcher:"+R.id.pager+":"+1);
            gMapFrag.updateTrackMap(TrackerActivity.getCurrentPoints());
            gMapFrag.fixBearing(TrackerActivity.getCurrentBearing());
            speed.setText(Double.toString(TrackerActivity.getCurrentSpeed()));
            distance.setText(Double.toString(TrackerActivity.getCurrentDistance()));
            myHandler.postDelayed(this, 0);
        }

    };
}