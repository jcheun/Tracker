package com.example.trackr;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;


public class GoogleMapActivity extends Fragment implements
        LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{

    private static final String LOG_TAG = "GoogleMapActivity";
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.activity_map, container, false);

        //
        mMapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.mapV);
        mMap = mMapFragment.getMap();
        mMap.setMyLocationEnabled(true);

        // Check if Google Play Service is Available
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if(status == ConnectionResult.SUCCESS){
            mLocationClient = new LocationClient(getActivity(), this, this);
            mLocationClient.connect();
        }

        Log.i(LOG_TAG,"Created View");
        return view;
    }

    @Override
    public void onResume() {
        Log.i(LOG_TAG,"onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(LOG_TAG,"onPause");
        if (mLocationClient != null) {
            mLocationClient.disconnect();
            Log.i(LOG_TAG,"GooglePlay Service Disconnected");
        }
        super.onPause();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location mLocation = mLocationClient.getLastLocation();
        CameraPosition myPosition = new CameraPosition.Builder()
                .target(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()))
                .zoom(15.5f)
                .bearing(0)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(myPosition), null);
        Log.i(LOG_TAG,"GooglePlay Service Connected");

        // Set GPS update interval
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    @Override
    public void onDisconnected() {
        mLocationClient.disconnect();
        Log.i(LOG_TAG,"GooglePlay Service Disconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOG_TAG,"GooglePlay Service Connection Failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location == null) return;
        TextView latitute = (TextView) getView().findViewById(R.id.latitute);
        TextView longitutde = (TextView) getView().findViewById(R.id.longitutde);
        TextView speed = (TextView) getView().findViewById(R.id.speed);
        latitute.setText(String.format("Latitute: %f",location.getLatitude()));
        longitutde.setText(String.format("Longitutde: %f", location.getLongitude()));
        speed.setText(String.format("Speed: %.2f meters/sec", location.getSpeed()));
    }
}