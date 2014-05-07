package com.example.trackr;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;


public class MapActivity extends Activity implements 
	LocationListener,
	GooglePlayServicesClient.ConnectionCallbacks,
	GooglePlayServicesClient.OnConnectionFailedListener {
	
	private GoogleMap myMap;
	private TextView latitute;
	private TextView longitutde;
	private TextView speed;
	private LocationClient myLocationClient;
	private LocationRequest myLocationRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		myMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapV)).getMap();
		myMap.setMyLocationEnabled(true);
		
		latitute = (TextView) findViewById(R.id.latitute);
		longitutde = (TextView) findViewById(R.id.longitutde);
		speed = (TextView) findViewById(R.id.speed);
		
		// Check if Google Play Service is Available
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if(status == ConnectionResult.SUCCESS){
			myLocationClient = new LocationClient(this, this, this);
			myLocationClient.connect();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// Move map to current location
		Location myLocation = myLocationClient.getLastLocation();
	    CameraPosition myPosition = new CameraPosition.Builder()
	    				.target(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
	                    .zoom(15.5f)
	                    .bearing(0)
	                    .build();
		
		myMap.animateCamera(CameraUpdateFactory.newCameraPosition(myPosition), null);
		
		
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
		if(location == null) return;
	    latitute.setText(String.format("Latitute: %f",location.getLatitude()));
	    longitutde.setText(String.format("Longitutde: %f",location.getLongitude()));
	    speed.setText(String.format("Speed: %.2f meters/sec", location.getSpeed()));
	}
}
