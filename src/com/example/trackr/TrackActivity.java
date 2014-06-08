package com.example.trackr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TrackActivity extends Fragment {
    private static final String LOG_TAG = "TrackActivity";

    private TextView textTimer;
    private Handler myHandler = new Handler();

    private Button btnStart;
    private Button btnStop;
    private boolean isRunning = false;
    private boolean background = false;
    private TrackerActivity activity;
    private String putRouteURL = "https://trackr121.appspot.com/trackr/default/putroute.json/behappy/";
    private SharedPreferences settings;
    // Sharedpref file name
    private static final String PREF_NAME = "AndroidHivePref";
    public String android_id;
    public data routeData;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        btnStop = (Button) view.findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                activity.stopService();
                settings = activity.getApplicationContext().getSharedPreferences(PREF_NAME, 0);
                android_id = Secure.getString(activity.getBaseContext().getContentResolver(),
                        Secure.ANDROID_ID);
                GoogleMapActivity gMapFrag = (GoogleMapActivity) getFragmentManager()
                        .findFragmentByTag(
                                "android:switcher:" + R.id.pager + ":" + 1);
                gMapFrag.saveRoute();
                routeData = HomeActivity.getCustomRoutes();
                BackgroundDownloader downloader = new BackgroundDownloader();

                putRouteURL = putRouteURL.concat(settings.getString("username", null)+"/")
                        .concat(android_id+"/");
                Log.d("url:",putRouteURL);
                downloader.execute(putRouteURL);

                getActivity().finish();

            }

        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (activity.isLogging()) {
                background = true;
                startService();
            }

            if (true) {
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
        if (!background) {
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
        GoogleMapActivity gMapFrag = (GoogleMapActivity) getFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.pager + ":" + 1);
        gMapFrag.updateTrackMap(activity.getCurrentPoints());
        gMapFrag.fixBearing(activity.getCurrentBearing(), activity.getCurrentPosition());
    }

    private void updateDisplay() {
        int seconds = (int) (activity.getFinalTime() / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;

        textTimer = (TextView) getView().findViewById(R.id.textTimer);
        textTimer.setText(String.format("%d : %02d", minutes, seconds));

        TextView speed = (TextView) getActivity().findViewById(
                R.id.currentSpeed);
        TextView distance = (TextView) getActivity().findViewById(
                R.id.distTravel);
        speed.setText(Double.toString(activity.getCurrentSpeed()));
        distance.setText(Double.toString(activity.getCurrentDistance()));
    }

    public static String POST(URI url, data routeData){
        InputStream inputStream = null;
        String result = "";
        try {
            Log.d("TrackActivity", "1");
            HttpClient httpclient = new DefaultHttpClient();
            Log.d("TrackActivity", "2");
            HttpPost httpPost = new HttpPost(url);
            Log.d("TrackActivity", "3");
            String json = "";

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("destination", routeData.destination);
            jsonObject.put("avg_speed", "0.0");
            jsonObject.put("distance", routeData.distance);
            jsonObject.put("max_speed", routeData.maxSpeed);
            Log.i(LOG_TAG, Double.toString( routeData.time));
            jsonObject.put("duration", routeData.time);
            jsonObject.put("route", routeData.route);
            jsonObject.put("start", routeData.start);
            jsonObject.put("tracked_route", routeData.trackedRoute);

            json = jsonObject.toString();
            Log.i(LOG_TAG, json);

            StringEntity se = new StringEntity(json);

            httpPost.setEntity(se);

            httpPost.setHeader("Accept", "application/json");

            httpPost.setHeader("Content-type", "application/json");

            HttpResponse httpResponse = httpclient.execute(httpPost);

            inputStream = httpResponse.getEntity().getContent();

            if(inputStream != null)
                result = ConvertStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("InputStream", "gfdsg");
        }

        // 11. return result
        return result;
    }

    private class BackgroundDownloader extends
            AsyncTask<String, String, String> {

        private static final int MAX_SETUP_DOWNLOAD_TRIES = 3;

        protected String doInBackground(String... urls) {
            Log.d(LOG_TAG, "Sending login request....");
            String downloadedString = null;
            String urlString = urls[0];
            URI url = URI.create(urlString);
            return POST(url,routeData);
        }

        // After making the HTTP request
        protected void onPostExecute(String s) {
            Context context = activity.getApplicationContext();
            Toast toast;
            CharSequence text;
            String result;
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(activity.getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
            Log.d("server response:", s);
        }
    }

    public static String ConvertStreamToString(InputStream is) {

        if (is == null) {
            return null;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append((line + "\n"));
            }
        } catch (IOException e) {
            Log.d(LOG_TAG, e.toString());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.d(LOG_TAG, e.toString());
            }
        }
        return sb.toString();
    }

}