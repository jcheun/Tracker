package com.example.trackr;

import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
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
import com.google.android.gms.maps.model.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class GoogleMapActivity extends Fragment implements
        LocationListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{

    private static final String LOG_TAG = "GoogleMapActivity";
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    private String startLocation = "";
    private String destLocation  = "";
    private Polyline myPolyline;
    LinkedHashMap<String,LatLng> points;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.activity_map, container, false);
        mMapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.mapV);
        mMap = mMapFragment.getMap();
        mMap.setMyLocationEnabled(true);

        mMap.setMyLocationEnabled(true);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);


        // Check if Google Play Service is Available
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if(status == ConnectionResult.SUCCESS){
            mLocationClient = new LocationClient(getActivity(), this, this);
            mLocationClient.connect();
        }
        points = new LinkedHashMap<String, LatLng>();
        myPolyline = mMap.addPolyline( new PolylineOptions()
                .color(0x770000FF));
        // Prevent Crash from accessing Http request
        // This is a bad method
        StrictMode.ThreadPolicy policy = new StrictMode.
                ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
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

    public void setStart(String start) {
        startLocation = start.replaceAll(" ", "%20");
    }

    public void setDestination(String destination) {
        destLocation = destination.replaceAll(" ", "%20");
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true));

        points.put(marker.getId(), latLng);
        updateRoute();
        //myPolyline.setPoints(new ArrayList<LatLng>(points.values()));

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        points.remove(marker.getId());
        marker.remove();
        updateRoute();
        //myPolyline.setPoints(new ArrayList<LatLng>(points.values()));
        return true;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        points.put(marker.getId(), marker.getPosition());
        updateRoute();
        //myPolyline.setPoints(new ArrayList<LatLng>(points.values()));
    }

    List<LatLng> cord = new ArrayList<LatLng>();
    public void decodePath(String path) {
        List<Double> list = new ArrayList<Double>();
        for(String a : path.split("(?<=[\\x00-\\x5E])")) {
            int point = 0;
            for(int i = 0; i < a.length(); ++i) {
                point += ((a.charAt(i) - 63) & 0x1F) << (i*5);
            }
            list.add((double)(((point & 0x01) == 1) ? ~(point >> 1): (point >> 1)) / 100000);
        }

        double latBase = 0, longBase = 0;
        for(int index = 0; index < list.size(); ++index) {
            cord.add(new LatLng(latBase += list.get(index), longBase += list.get(++index)));
        }
    }


    public String getJson(String url) {
        URL mUrl;
        HttpURLConnection mConnection = null;
        StringBuilder json = new StringBuilder();

        try {
            mUrl = new URL(url);
            mConnection = (HttpURLConnection)mUrl.openConnection();
            BufferedReader input = new BufferedReader(new InputStreamReader(mConnection.getInputStream()));
            String line;
            while((line = input.readLine()) != null) {
                json.append(line);
            }
            input.close();
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            e.printStackTrace();
        }

        if(mConnection != null) mConnection.disconnect();

        Log.i(LOG_TAG, json.toString());
        return json.toString();
    }

    public void updateRoute() {


        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json");
        url.append("?origin=" + startLocation);
        url.append("&destination=" + destLocation);
        url.append("&waypoints=optimize:true%7C");
        for(LatLng latlng : points.values()) {
            url.append(latlng.toString().replaceAll("[^0-9,.-]","") + "%7C");
        }
        url.append("&sensor=true");

        //Log.i(LOG_TAG, url.toString());

        String jsonResults = getJson(url.toString());

        try {
            // Parse Json string
            JSONObject object = new JSONObject(jsonResults);
            JSONArray routes = object.getJSONArray("routes");
            JSONObject route = routes.getJSONObject(0);

            JSONArray legs = route.getJSONArray("legs");
            cord.clear();
            for (int i = 0; i < legs.length(); ++i) {
                JSONObject leg = legs.getJSONObject(i);
                JSONArray steps = leg.getJSONArray("steps");

                for (int index = 0; index < steps.length(); ++index) {
                    JSONObject step = steps.getJSONObject(index);
                    JSONObject polyline = step.getJSONObject("polyline");
                    if (polyline.has("points")) {
                        decodePath(polyline.getString("points"));
                    }
                }
            }
            myPolyline.setPoints(cord);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
            e.printStackTrace();
        }
    }
}