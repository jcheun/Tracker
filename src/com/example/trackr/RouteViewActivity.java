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
import java.util.List;

import android.app.Activity;
import com.google.android.gms.maps.model.LatLng;
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

public class RouteViewActivity extends Fragment {
	private static final String LOG_TAG = "TrackActivity";

	private TextView textToEdit;
	private RouteInfoActivity activity;
	private SharedPreferences settings;
	// Sharedpref file name
    private static final String PREF_NAME = "AndroidHivePref";
    public data routeData;
    
	public static RouteViewActivity newInstance(String title) {
		RouteViewActivity routeViewFrag = new RouteViewActivity();
		Bundle bundle = new Bundle();
		bundle.putString("Title", title);
		routeViewFrag.setArguments(bundle);
		return routeViewFrag;
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
        activity = (RouteInfoActivity) getActivity();
        routeData = activity.routeData;
        View view = inflater.inflate(R.layout.activity_route_view, container, false);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		textToEdit = (TextView) getActivity().findViewById(R.id.dateData);
		textToEdit.setText(routeData.sDate);
		textToEdit = (TextView) getActivity().findViewById(R.id.distanceData);
		textToEdit.setText(routeData.sDistance);
		textToEdit = (TextView) getActivity().findViewById(R.id.avgSpeedData);
		textToEdit.setText(routeData.sAvgSpeed);
		textToEdit = (TextView) getActivity().findViewById(R.id.maxSpeedData);
		textToEdit.setText(routeData.sMaxSpeed);
		textToEdit = (TextView) getActivity().findViewById(R.id.durationData);
        int seconds = Integer.valueOf(routeData.sDuration) / 1000;
        int minutes = seconds / 60;
        int hours = minutes/60;
        seconds = seconds % 60;

		textToEdit.setText(String.format("%02d : %02d : %02d", hours, minutes, seconds));
		
////		String trackedRoute = routeData.trackedRoute;
//		String savedRoute = routeData.route;
//        Log.i("RouteV", savedRoute);
//        List<LatLng> list = GoogleHelper.decodePath(savedRoute);
//        LatLng a = list.get(0);
//        Log.i("afsadf", a.toString());
//		GoogleMapActivity gMapFrag = (GoogleMapActivity) getFragmentManager().findFragmentByTag("android:switcher:"+R.id.pager+":"+1);
////		gMapFrag.updateTrackMap(GoogleHelper.decodePath(trackedRoute));
//        while(!gMapFrag.isReady());
//        gMapFrag.updateRouteMap(list);
	}

	@Override
	public void onPause() {
		super.onPause();
		
	}

	

}