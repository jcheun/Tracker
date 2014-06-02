package com.example.trackr;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		appInfo = AppInfo.getInstance(this);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
        customRoutes = new ArrayList<data>();
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
		Intent intent = new Intent(this, CreateRouteActivity.class);
		startActivity(intent);
	}
	
	public void mapView(View V) {
		// Go to second activity
		Intent intent = new Intent(this, TrackerActivity.class);
		startActivity(intent);
	}

	public void viewProfile(View V) {
        Intent intent = new Intent(this, test.class);
        startActivity(intent);
	}

	public void goSettings(View V) {

	}

    public static void setCustomRoutes(data route){
        customRoutes.add(route);
    }

    public static void getCustomRoutes() {
        if(customRoutes.isEmpty()) return;
        data tmp = customRoutes.get(0);
        Log.i("Home", tmp.route);
    }
	
}
