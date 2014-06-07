package com.example.trackr;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.*;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class TrackerService extends Service implements
        LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{

    private static final String LOG_TAG = "TrackerService";

    private LocationClient mLocationClient;

    private List<LatLng> mPoints;
    private List<Double> speeds;
    private List<Double> altitudes;
    private Location pLocation = null;

    private double cSpeed = 0;
    private double cDistance = 0;
    private float cBearing = 0;

    private boolean running = false;
    private boolean logging = false;


    private long startTime = 0L;
    private long timeMilli = 0L;
    private long timeSwap = 0L;
    private long finalTime = 0L;

    IBinder mBinder = new MyBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (running) return START_STICKY;
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        showMyNotification();

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (status == ConnectionResult.SUCCESS) {
            mLocationClient = new LocationClient(getApplicationContext(), this, this);
            mLocationClient.connect();
        }
        speeds = new ArrayList<Double>();
        altitudes = new ArrayList<Double>();
        mPoints = new ArrayList<LatLng>();
        running = true;
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) return;
        cSpeed = location.getSpeed();
        if(mPoints.isEmpty()) {
            pLocation = location;
//            Marker marker = mMap.addMarker(new MarkerOptions()
//                    .position(new LatLng(location.getLatitude(), location.getLongitude())));
            mPoints.add(new LatLng(location.getLatitude(), location.getLongitude()));
            mPoints.add(new LatLng(location.getLatitude(), location.getLongitude()));
        } else {

            if(location.distanceTo(pLocation) >= 10) {
                mPoints.add(new LatLng(location.getLatitude(), location.getLongitude()));
                cBearing = location.bearingTo(pLocation);
                if (cBearing <180) cBearing+= 180;
                cDistance += location.distanceTo(pLocation);
                speeds.add(cSpeed);
                altitudes.add(location.getAltitude());
                pLocation = location;
            } else {
                mPoints.set(mPoints.size()-1,new LatLng(location.getLatitude(), location.getLongitude()));
            }
        }
    }


    public long getTime() {
        timeMilli = SystemClock.uptimeMillis() - startTime;
        return finalTime = timeSwap + timeMilli;
    }

    public List<Double> getSpeeds() {
        return speeds;
    }

    public List<Double> getAltitudes() {
        return altitudes;
    }

    public List<LatLng> getCurrentPoints() {
        return mPoints;
    }

    public long getFinalTime() {
        return finalTime;
    }

    public double getCurrentSpeed() {
        return cSpeed;
    }

    public double getCurrentDistance() {
        return cDistance;
    }

    public float getCurrentBearing() {
        return cBearing;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isLogging() {
        return logging;
    }

    public void enableLogging(Boolean enable) {
        logging = enable;
        if(enable)
            startTime = SystemClock.uptimeMillis();
        else
            timeSwap += timeMilli;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOG_TAG, "Location Service Connected");
        setGPSInterval(1000);
    }

    @Override
    public void onDisconnected() {
        mLocationClient.disconnect();
        Log.i(LOG_TAG, "Location Service Disconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "Location Service Connection Failed");
    }

    public class MyBinder extends Binder {
        public TrackerService getServicesInstance() {
            return TrackerService.this;
        }
    }

    public void setGPSInterval(int interval) {
        // Set GPS update interval
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(interval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    /**
     * Show a notification while this service is running.
     */
    @SuppressWarnings("deprecation")
    private void showMyNotification() {

        // Creates a notification.
        Notification notification = new Notification(
                R.drawable.ic_launcher,
                "Tracking",
                System.currentTimeMillis());

        Intent notificationIntent = new Intent(this, TrackerService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        notification.setLatestEventInfo(this, "Appp",
                "Tracking", pendingIntent);
        startForeground(1, notification);
    }
}
