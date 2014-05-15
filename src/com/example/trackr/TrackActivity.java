package com.example.trackr;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class TrackActivity extends Activity implements LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private GoogleMap myMap;
	private TextView latitute;
	private TextView longitutde;
	private TextView speed;
	private LocationClient myLocationClient;
	private LocationRequest myLocationRequest;

	AppInfo appInfo;

	private TextView textTimer;
	private ViewFlipper viewFlipper;

	private Handler myHandler = new Handler();
	private long startTime = 0L;
	private long timeMilli = 0L;
	private long timeSwap = 0L;
	private long finalTime = 0L;
	private boolean isRunning = false;

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
	private Animation slideRightOut;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_track);
		viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper1);
		viewFlipper.setDisplayedChild(2);
		myMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapV))
				.getMap();
		myMap.setMyLocationEnabled(true);

		latitute = (TextView) findViewById(R.id.latitute);
		longitutde = (TextView) findViewById(R.id.longitutde);
		speed = (TextView) findViewById(R.id.speed);

		// Check if Google Play Service is Available
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (status == ConnectionResult.SUCCESS) {
			myLocationClient = new LocationClient(this, this, this);
			myLocationClient.connect();
		}

		slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.in_from_right);
		slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.out_to_left);
		slideRightIn = AnimationUtils.loadAnimation(this, R.anim.in_from_left);
		slideRightOut = AnimationUtils.loadAnimation(this, R.anim.out_to_right);

		gestureDetector = new GestureDetector(new MyGestureDetector());
		gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (gestureDetector.onTouchEvent(event)) {
					return true;
				}
				return false;
			}
		};
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

	class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;

				// right to left swipe
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					if (viewFlipper.getDisplayedChild() == 1) {
					} else {
						viewFlipper.setInAnimation(slideLeftIn);
						viewFlipper.setOutAnimation(slideLeftOut);
						viewFlipper.showNext();
					}
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					if (viewFlipper.getDisplayedChild() == 0) {
					} else {
					viewFlipper.setInAnimation(slideRightIn);
					viewFlipper.setOutAnimation(slideRightOut);
					viewFlipper.showPrevious();
					}
				}
			} catch (Exception e) {
				// nothing
			}
			return false;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector.onTouchEvent(event))
			return true;
		else
			return false;
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

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// Move map to current location
		Location myLocation = myLocationClient.getLastLocation();
		CameraPosition myPosition = new CameraPosition.Builder()
				.target(new LatLng(myLocation.getLatitude(), myLocation
						.getLongitude())).zoom(15.5f).bearing(0).build();

		myMap.animateCamera(CameraUpdateFactory.newCameraPosition(myPosition),
				null);

		// Set GPS update interval
		myLocationRequest = LocationRequest.create();
		myLocationRequest.setInterval(1000);
		myLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		myLocationClient.requestLocationUpdates(myLocationRequest, this);
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		myLocationClient.disconnect();
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location == null)
			return;
		latitute.setText(String.format("Latitute: %f", location.getLatitude()));
		longitutde.setText(String.format("Longitutde: %f",
				location.getLongitude()));
		speed.setText(String.format("Speed: %.2f meters/sec",
				location.getSpeed()));
	}

}
