package com.example.trackr;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class SplashActivity extends Activity {
	
	public boolean loggedIn = false;
	private SharedPreferences settings;
	// Sharedpref file name
    private static final String PREF_NAME = "AndroidHivePref";
	private static final String LOG_TAG = "splash screen";
	Handler myHandler;
	Intent intent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		intent = new Intent(this, LoginActivity.class);
		settings = getApplicationContext().getSharedPreferences(PREF_NAME, MODE_PRIVATE);
		myHandler = new Handler();
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
		myHandler.postDelayed(new Runnable() { 

		@Override
		public void run() {
			startActivity(intent);
		} }, 2000);
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

}
