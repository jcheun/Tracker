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

    private Button btnStart;
    private boolean isRunning = false;
    private boolean background = false;
    private TrackerActivity activity;

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

        activity = (TrackerActivity) getActivity();
        btnStart = (Button) view.findViewById(R.id.Start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textTimer = (TextView) getView().findViewById(R.id.textTimer);
                if (isRunning == false) {
                    startService();
                } else {
                    pauseService();
                }
            }
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if(activity.isLogging()) {
                background = true;
                startService();
            }

            if(true) {
                updateDisplay();
                updateMap();
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        myHandler.removeCallbacks(updateTimerMethod);
    }

    public void startService() {
        isRunning = true;
        btnStart.setText("Pause");
        if(!background) {
            activity.enableLogging(true);
        }
        background = false;
        myHandler.postDelayed(updateTimerMethod, 0);
    }

    public void pauseService() {
        isRunning = false;
        btnStart.setText("Start");
        activity.enableLogging(false);
        myHandler.removeCallbacks(updateTimerMethod);
    }

    private Runnable updateTimerMethod = new Runnable() {

        public void run() {
            updateMap();
            updateDisplay();
            myHandler.postDelayed(this, 1000);
        }

    };

    private void updateMap() {
        activity.getTime();
        GoogleMapActivity gMapFrag = (GoogleMapActivity) getFragmentManager().findFragmentByTag("android:switcher:"+R.id.pager+":"+1);
        gMapFrag.updateTrackMap(activity.getCurrentPoints());
        gMapFrag.fixBearing(activity.getCurrentBearing());
    }

    private void updateDisplay() {
        int seconds = (int) (activity.getFinalTime() / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;

        textTimer = (TextView) getView().findViewById(R.id.textTimer);
        textTimer.setText(String.format("%d : %02d", minutes, seconds));

        TextView speed = (TextView) getActivity().findViewById(R.id.currentSpeed);
        TextView distance = (TextView) getActivity().findViewById(R.id.distTravel);
        speed.setText(Double.toString(activity.getCurrentSpeed()));
        distance.setText(Double.toString(activity.getCurrentDistance()));
    }
}