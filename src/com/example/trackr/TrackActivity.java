package com.example.trackr;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class TrackActivity extends Activity {

	AppInfo appInfo;

	private TextView textTimer;
	private Handler myHandler = new Handler();
	private long startTime = 0L;
	private long timeMilli = 0L;
	private long timeSwap = 0L;
	private long finalTime = 0L;
	private boolean isRunning = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_track);
		appInfo = AppInfo.getInstance(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public void goBack(View V) {
		Intent intent = new Intent(this, HomeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		// finish();+
	}

	public void startStop(View V) {
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

	private Runnable updateTimerMethod = new Runnable() {

		public void run() {

			timeMilli = SystemClock.uptimeMillis() - startTime;
			finalTime = timeSwap + timeMilli;
			textTimer = (TextView) findViewById(R.id.textTimer);
			int seconds = (int) (finalTime / 1000);
			int minutes = seconds / 60;
			seconds = seconds % 60;
			int milliseconds = (int) (finalTime % 1000);
			textTimer.setText("" + minutes + ":"
					+ String.format("%02d", seconds) + ":"
					+ String.format("%03d", milliseconds));
			myHandler.postDelayed(this, 0);
		}

	};

}
