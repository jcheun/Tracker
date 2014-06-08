package com.example.trackr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.trackr.HomeActivity.PlaceholderFragment;

public class LoginActivity extends Activity {

	private String user;
	private String pass;
	private final String key = "behappy";
	private String loginURL = "https://trackr121.appspot.com/trackr/default/login.json/";
	private static final int MAX_SETUP_DOWNLOAD_TRIES = 3;
	private static final String LOG_TAG = "loginPoster";
	public boolean loggedIn;
	private SharedPreferences settings;
	// Sharedpref file name
    private static final String PREF_NAME = "AndroidHivePref";
	
    public String android_id;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		settings = getApplicationContext().getSharedPreferences(PREF_NAME, MODE_PRIVATE);
		checkLoggedIn();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_home, container,
					false);
			return rootView;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkLoggedIn();
	}

	public void login(View v) {
		EditText userTxt = (EditText) findViewById(R.id.username);
		EditText passTxt = (EditText) findViewById(R.id.password);
		user = userTxt.getText().toString();
		pass = passTxt.getText().toString();
		android_id = Secure.getString(getBaseContext().getContentResolver(), 
				Secure.ANDROID_ID);
		BackgroundDownloader downloader = new BackgroundDownloader();
		String updatedURL = loginURL.concat("behappy/").concat(user).concat("/")
										.concat(android_id).concat("/").concat(pass);
		downloader.execute(updatedURL);
		
	}

	public void register(View v) {
		Intent intent = new Intent(this, RegisterActivity.class);
		startActivity(intent);
	}
	
	public void checkLoggedIn(){
		Log.d(LOG_TAG, "checking if logged in ..." + loggedIn);
		loggedIn = settings.getBoolean("loggedIn", false);
		if(loggedIn == true){
			Log.d(LOG_TAG, "logged in ..." + loggedIn);
			Intent intent = new Intent(this, HomeActivity.class);
			startActivity(intent);
		}
	}

	private class BackgroundDownloader extends
			AsyncTask<String, String, String> {

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
			Context context = getApplicationContext();
			Toast toast;
			CharSequence text;
			String result;
			int duration = Toast.LENGTH_SHORT;
			
			try{
				JSONObject jsonObj = new JSONObject(s);
				text = jsonObj.getString("result");
				toast = Toast.makeText(context, text, duration);
				toast.setGravity(Gravity.BOTTOM|Gravity.CENTER,0,0);
				toast.show();
				if(text.toString().compareTo("Login Success") == 0){
					Log.d(LOG_TAG, "Logging in");
					Editor editor = settings.edit();
					editor.putBoolean("loggedIn",true);
					editor.putString("username", user);
					editor.commit();
				}
				checkLoggedIn();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static String ConvertStreamToString(InputStream is){
		
		if(is == null){
			return null;
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		
		String line = null;
		try{
			while((line = reader.readLine()) != null){
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
