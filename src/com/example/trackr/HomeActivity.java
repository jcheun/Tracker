package com.example.trackr;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends Activity {

	AppInfo appInfo;

    private static List<data> customRoutes;
    private static List<data> trackedRoutes;
    private SharedPreferences settings;
	// Sharedpref file name
    private static final String PREF_NAME = "AndroidHivePref";
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		appInfo = AppInfo.getInstance(this);
		settings = getApplicationContext().getSharedPreferences(PREF_NAME, MODE_PRIVATE);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
        customRoutes = new ArrayList<data>();
        trackedRoutes = new ArrayList<data>();
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
			View rootView = inflater.inflate(R.layout.fragment_home, container, false);
			return rootView;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	public void startTrack(View V) {
		// Go to second activity
        Intent intent = new Intent(this, TrackerActivity.class);
        startActivity(intent);
	}
	
	public void mapView(View V) {
		// Go to second activity
        Intent intent = new Intent(this, CreateRouteActivity.class);
        startActivity(intent);
	}

	public void viewProfile(View V) {
        Intent intent = new Intent(this, ListRoute.class);
        startActivity(intent);
	}

	public void goSettings(View V) {

	}

    public static void setCustomRoutes(data route){
        customRoutes.add(route);
    }

    public static void setTrackedRoutes(data route) {
        trackedRoutes.add(route);
    }

    public static data getCustomRoutes(int index) {
        if(customRoutes.isEmpty()) return null;
        data tmp = customRoutes.get(index);
        Log.i("Home get custom", tmp.route);
        return tmp;
    }

    public static data getLCustomRoutes() {
        if(customRoutes.isEmpty()) return null;
        data tmp = customRoutes.get(customRoutes.size() - 1);
        Log.i("Home get custom", tmp.route);
        return tmp;
    }

    public static List<data> getAllRoutes() {
        if(trackedRoutes.isEmpty()) return null;
        return trackedRoutes;
    }

    public static data getTrackedRoutes() {
        if(trackedRoutes.isEmpty()) return null;
        data tmp = trackedRoutes.get(trackedRoutes.size() - 1);
        Log.i("Home get tracked", tmp.trackedRoute);
        return tmp;
    }
    
    public static data getTrackedRoute(int index) {
    	if(trackedRoutes.isEmpty()) return null;
    	data tmp = trackedRoutes.get(index);
    	return tmp;
    }
    
    public void clickLogout(View v){
    	Editor editor = settings.edit();
		editor.clear();
		editor.commit();
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
    }
	
}
