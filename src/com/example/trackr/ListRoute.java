package com.example.trackr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ListRoute extends Activity {
	private static final String LOG_TAG = null;
	private List<data> rdata;
	private ListView list;
	private static String DOWNLOAD_URL = "https://trackr121.appspot.com/trackr/default/getallroutes.json/behappy/";
	private SharedPreferences settings;
	// Sharedpref file name
    private static final String PREF_NAME = "AndroidHivePref";
    public String android_id;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_layout);
		rdata = new ArrayList<data>();
		list = (ListView) findViewById(R.id.listView);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int i, long l) {
				Toast.makeText(getApplicationContext(),
						"Click ListItem Number " + i, Toast.LENGTH_LONG).show();

				Intent intent = new Intent(getApplicationContext(),
						RouteInfoActivity.class);
				intent.putExtra("dataIndex", i);
				startActivity(intent);
			}
		});
		if (rdata != null) {
            refreshRoutes();
			updateRoutes();
		}
	}

	public void updateRoutes() {
		CustomAdapter adapter = new CustomAdapter(this, rdata);
		list.setAdapter(adapter);
	}

	public void refreshRoutes() {
		settings = getApplicationContext().getSharedPreferences(PREF_NAME, 0);
		android_id = Secure.getString(getBaseContext().getContentResolver(), 
				Secure.ANDROID_ID);
		BackgroundDownloader downloader = new BackgroundDownloader();
		DOWNLOAD_URL = DOWNLOAD_URL.concat(settings.getString("username", null)+"/")
				.concat(android_id+"/");
		Log.d("executing request:", DOWNLOAD_URL);
		downloader.execute(DOWNLOAD_URL);
		
	}

	public class CustomAdapter extends ArrayAdapter<data> {

		Context mContext;
		List<data> objects = null;

		public CustomAdapter(Context context, List<data> object) {
			super(context, R.layout.item_layout, object);
			this.mContext = context;
			this.objects = object;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			View view = inflater.inflate(R.layout.item_layout, parent, false);

			TextView s1 = (TextView) view.findViewById(R.id.itemFrom);
			TextView s2 = (TextView) view.findViewById(R.id.itemTo);
            TextView s3 = (TextView) view.findViewById(R.id.itemDate);
            TextView s4 = (TextView) view.findViewById(R.id.itemTime);

			s1.setText(objects.get(position).sStart);
			s2.setText(objects.get(position).sDestination);
            s3.setText(objects.get(position).sDate.substring(0,10));
            int seconds = Integer.valueOf(objects.get(position).sDuration) / 1000;
            int minutes = seconds / 60;
            int hours = minutes/60;
            seconds = seconds % 60;

            s4.setText(String.format("%02d : %02d : %02d", hours, minutes, seconds));
			return view;
		}
	}

	private class BackgroundDownloader extends
			AsyncTask<String, String, String> {

		private static final String LOG_TAG = "getrouteDL";

		protected String doInBackground(String... urls) {
			Log.d(LOG_TAG, "Sending login request....");
			String downloadedString = null;
			String urlString = urls[0];
			URI url = URI.create(urlString);
			int numTries = 0;
			while (downloadedString == null
					&& numTries < 3 && !isCancelled()) {
				numTries++;
				HttpGet request = new HttpGet(url);
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
			Context context = getApplicationContext();
			Toast toast;
			JSONArray routes = null;
			String result;
			int duration = Toast.LENGTH_SHORT;

			try {
				JSONObject jsonObj = new JSONObject(s);
				routes = jsonObj.getJSONArray("routes");
				for (int i = 0; i < routes.length(); i++){
					JSONObject route = routes.getJSONObject(i);
					data routeData = new data();
					routeData.setServerData(route);
					rdata.add(routeData);
					HomeActivity.setTrackedRoutes(routeData);
					Log.d("rdata size:", rdata.size()+"");
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			updateRoutes();
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