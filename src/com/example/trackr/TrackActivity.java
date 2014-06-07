package com.example.trackr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
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
	private String loginURL = "https://trackr121.appspot.com/trackr/default/put.json/";
	private SharedPreferences settings;
	// Sharedpref file name
    private static final String PREF_NAME = "AndroidHivePref";
	
    public String android_id;

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
				data routeData = HomeActivity.getTrackedRoutes();
				double avg_speed = routeData.avgSpeed;
				double max_speed = routeData.maxSpeed;
				double distance = routeData.distance;
				int duration = routeData.time;
				String start = routeData.start;
				String destination = routeData.destination;
				String tracked_route = routeData.trackedRoute;
				String route = routeData.route;
//				JSONObject json = new JSONObject();
//				try {
//					json.put("destination", routeData.destination);
//					json.put("avg_speed", routeData.avgSpeed);
//					json.put("distance", routeData.distance);
//					json.put("max_speed", routeData.maxSpeed);
//					json.put("duration", routeData.time);
//					json.put("route", routeData.route);
//					json.put("start", routeData.start);
//					json.put("tracked_route", routeData.trackedRoute);
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
				BackgroundDownloader downloader = new BackgroundDownloader();
				String updatedURL = loginURL.concat("behappy/").concat(settings.getString("username", null)+"/")
												.concat(android_id+"/").concat(avg_speed+"/").concat(max_speed+"/")
												.concat(distance+"/").concat(duration+"/")
												.concat(start+"/").concat(destination+"/")
												.concat(route+"/").concat(tracked_route+"/");
				Log.d("URL:", updatedURL);
				String safeUrl = URLEncoder.encode(updatedURL);
				downloader.execute(safeUrl);
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
		gMapFrag.fixBearing(activity.getCurrentBearing());
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

	private class BackgroundDownloader extends
			AsyncTask<String, String, String> {

		private static final int MAX_SETUP_DOWNLOAD_TRIES = 3;

		protected String doInBackground(String... urls) {
			Log.d(LOG_TAG, "Sending login request....");
			String downloadedString = null;
			String urlString = urls[0];
			URI url = URI.create(urlString);
			int numTries = 0;
			while (downloadedString == null
					&& numTries < MAX_SETUP_DOWNLOAD_TRIES && !isCancelled()) {
				numTries++;
				HttpPost request = new HttpPost(url);
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpResponse response = null;
				try {
					response = httpClient.execute(request);
				} catch (ClientProtocolException ex) {
					Log.e(LOG_TAG, ex.toString());
				} catch (IOException ex) {
					Log.e(LOG_TAG, ex.toString());
				}
				if (response != null) {
					// Checks the status code.
					int statusCode = response.getStatusLine().getStatusCode();
					Log.d(LOG_TAG, "Status code: " + statusCode);

					if (statusCode == HttpURLConnection.HTTP_OK) {
						// Correct response. Reads the real result.
						// Extracts the string content of the response.
						HttpEntity entity = response.getEntity();
						InputStream iStream = null;
						try {
							iStream = entity.getContent();
						} catch (IOException ex) {
							Log.e(LOG_TAG, ex.toString());
						}
						if (iStream != null) {
							downloadedString = ConvertStreamToString(iStream);
							Log.d(LOG_TAG, "Received string: "
									+ downloadedString);
							return downloadedString;
						}
					}
				}
			}
			// Returns the instructions, if any.
			return downloadedString;
		}

		// After making the HTTP request
		protected void onPostExecute(String s) {
			Context context = activity.getApplicationContext();
			Toast toast;
			CharSequence text;
			String result;
			int duration = Toast.LENGTH_SHORT;

			try {
				JSONObject jsonObj = new JSONObject(s);
				text = jsonObj.getString("result");
				toast = Toast.makeText(context, text, duration);
				toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 0);
				toast.show();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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