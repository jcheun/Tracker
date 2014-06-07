package com.example.trackr;


import android.content.Intent;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class GoogleMapActivity extends Fragment implements
        LocationListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private static final String LOG_TAG = "GoogleMapActivity";
    private GoogleMap mMap;
    private Geocoder  mGeocoder;
    private LocationClient mLocationClient;
    private LinkedHashMap<String, LatLng> wayPoints;
    private List<LatLng> mPoints;
    private List<Double> mSpeed;
    private List<Double> mAltitude;


    private Marker sMarker = null;
    private Marker dMarker = null;
    private Location pLocation = null;
    private Polyline mPolyline;
    private Polyline rPolyline;
    private String sLocation = "My Location";
    private String dLocation = "Destination";
    private String rUrl="";
    private Boolean editMode;


    private static double cDistance = 0;
    private static double cSpeed = 0;



    public static GoogleMapActivity newInstance(Boolean editMode, String route) {
        GoogleMapActivity gMapFrag = new GoogleMapActivity();
        Bundle bundle = new Bundle();
        bundle.putBoolean("EditMode", editMode);
        bundle.putString("Route", route);
        gMapFrag.setArguments(bundle);
        return gMapFrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view;
        SupportMapFragment mMapFragment;

        if (getArguments().getBoolean("EditMode")) {
            view = inflater.inflate(R.layout.activity_map_edit, container, false);
            mMapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.mapV);

            Button track = (Button) view.findViewById(R.id.startTracking);
            track.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                	saveRoute();
                	getActivity().finish();                
                    Intent intent = new Intent(getActivity(), TrackerActivity.class);
                    intent.putExtra("URL", rUrl);
                    startActivity(intent);
                }
            });

            Button save = (Button) view.findViewById(R.id.saveRoute);
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveRoute();
                }
            });

        } else {
            view = inflater.inflate(R.layout.activity_map, container, false);
            mMapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.mapV2);
        }

        editMode = getArguments().getBoolean("EditMode");

        mMap = mMapFragment.getMap();
        mMap.setMyLocationEnabled(true);

        // Check if Google Play Service is Available
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (status == ConnectionResult.SUCCESS) {
            mLocationClient = new LocationClient(getActivity(), this, this);
            mLocationClient.connect();
        }


        mSpeed    = new ArrayList<Double>();
        mAltitude = new ArrayList<Double>();
        mPoints   = new ArrayList<LatLng>();
        mGeocoder = new Geocoder(getActivity());
        wayPoints = new LinkedHashMap<String, LatLng>();
        rPolyline = mMap.addPolyline(new PolylineOptions().color(0x770000FF));
        mPolyline = mMap.addPolyline(new PolylineOptions().color(Color.RED));

        // Prevent Crash from accessing Http request
        // This is a bad method
        StrictMode.ThreadPolicy policy = new StrictMode.
                ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        enableEditMode(editMode);
        if(!editMode) {
            updateMap(getArguments().getString("Route"));
        }
        Log.i(LOG_TAG, "Created View");
        return view;
    }

    @Override
    public void onResume() {
        Log.i(LOG_TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(LOG_TAG, "onPause");
        if (mLocationClient != null) {
            mLocationClient.disconnect();
            Log.i(LOG_TAG, "GooglePlay Service Disconnected");
        }
        super.onPause();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOG_TAG, "GooglePlay Service Connected");
        Location mLocation = mLocationClient.getLastLocation();
        GoogleHelper.moveCamera(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()),
                                0.0f, 15.5f, mMap);
//        if(!getArguments().getBoolean("EditMode")) {
//            setGPSInterval(1000);
//        }
    }

    @Override
    public void onDisconnected() {
        mLocationClient.disconnect();
        Log.i(LOG_TAG, "GooglePlay Service Disconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "GooglePlay Service Connection Failed");
    }

    @Override
    public void onLocationChanged(Location location) {
//        if (location == null) return;
//        cSpeed = location.getSpeed();
//        if(mPoints.isEmpty()) {
//            pLocation = location;
//            Marker marker = mMap.addMarker(new MarkerOptions()
//                                .position(new LatLng(location.getLatitude(), location.getLongitude())));
//            mPoints.add(marker.getPosition());
//            mPoints.add(marker.getPosition());
//        } else {
//
//            float bearing = location.bearingTo(pLocation);
//            if (bearing <180) bearing+= 180;
//
//            //TextView distance = (TextView) getActivity().findViewById(R.id.Distance);
//            //distance.setText("Distance: " + Double.toString(location.distanceTo(pLocation)*3.2808) + "ft");
//
//            if(location.distanceTo(pLocation) >= 10) {
//                mPoints.add(new LatLng(location.getLatitude(), location.getLongitude()));
//                GoogleHelper.moveCamera(new LatLng(location.getLatitude(), location.getLongitude()),
//                                        bearing , mMap.getCameraPosition().zoom, mMap);
//                cDistance += location.distanceTo(pLocation);
//                mSpeed.add(cSpeed);
//                mAltitude.add(location.getAltitude());
//                pLocation = location;
//            } else {
//                mPoints.set(mPoints.size()-1,new LatLng(location.getLatitude(), location.getLongitude()));
//            }
//        }
//
//        mPolyline.setPoints(mPoints);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (sMarker == null) {
            sLocation = GoogleHelper.geoToAdress(latLng, mGeocoder);
            sMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .draggable(true));
            updateAddress();
        } else if (dMarker == null) {
            dLocation = GoogleHelper.geoToAdress(latLng, mGeocoder);
            updateAddress();
            updateRoute();
        } else {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .draggable(true));
            wayPoints.put(marker.getId(), latLng);
            updateRoute();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.getId().equals(sMarker.getId())) return true;
        if (marker.getId().equals(dMarker.getId())) return true;

        wayPoints.remove(marker.getId());
        marker.remove();
        updateRoute();

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
        if (marker.getId().equals(sMarker.getId())) {
            sLocation = GoogleHelper.geoToAdress(marker.getPosition(), mGeocoder);
            updateAddress();
        } else if (marker.getId().equals(dMarker.getId())) {
            dLocation = GoogleHelper.geoToAdress(marker.getPosition(), mGeocoder);
            updateAddress();
        } else {
            wayPoints.put(marker.getId(), marker.getPosition());
        }
        updateRoute();
    }

    public void updateRoute() {
        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json");
        url.append("?origin=" + sLocation.replaceAll(" ", "%20"));
        url.append("&destination=" + dLocation.replaceAll(" ", "%20"));
        url.append("&waypoints=optimize:true%7C");
        for (LatLng latlng : wayPoints.values()) {
            url.append(latlng.toString().replaceAll("[^0-9,.-]", "") + "%7C");
        }
        url.append("&sensor=true");

        rUrl = url.toString();
        updateMap(url.toString());
    }

    public void updateTrackMap(List<LatLng> point) {
        mPolyline.setPoints(point);
    }

    public void updateMap (String url) {

        String jsonResults = GoogleHelper.getJson(url);
        List<LatLng> points = GoogleHelper.parseJson(jsonResults);
        if (sMarker != null) sMarker.remove();
        sMarker = null;
        if (dMarker != null) dMarker.remove();
        dMarker = null;

        if (points.size() >= 1) {
            sMarker = mMap.addMarker(new MarkerOptions()
                    .position(points.get(0))
                    .draggable(editMode));
        }

        if (points.size() >= 2) {
            dMarker = mMap.addMarker(new MarkerOptions()
                    .position(points.get(points.size() - 1))
                    .draggable(editMode));
        }

        rPolyline.setPoints(points);
    }

    public void setAddress(String start, String destination) {
        if (start.equalsIgnoreCase("My Location")) {
            LatLng mLocation = new LatLng(mLocationClient.getLastLocation().getLatitude(),
                                          mLocationClient.getLastLocation().getLongitude());
            sLocation = GoogleHelper.geoToAdress(mLocation, mGeocoder);
        } else {
            sLocation = start;
        }


        dLocation = destination;
        updateAddress();
        updateRoute();
        fixCamera();
    }

    public void fixBearing(float bearing) {
        GoogleHelper.moveCamera(mMap.getCameraPosition().target,
                     bearing , mMap.getCameraPosition().zoom, mMap);
    }

    // fix camera to show full route
    public void fixCamera() {
        if (sMarker != null && dMarker != null) {
            LatLngBounds.Builder bound = new LatLngBounds.Builder();
            bound.include(sMarker.getPosition());
            bound.include(dMarker.getPosition());
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bound.build(), 50));
        }
    }


    public void updateAddress() {
        RouteSearchActivity rSearchFrag = (RouteSearchActivity) getFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.pager + ":" + 0);
        View rSearchView = rSearchFrag.getView();
        AutoCompleteTextView auto_from = (AutoCompleteTextView) rSearchView.findViewById(R.id.from);
        AutoCompleteTextView auto_to   = (AutoCompleteTextView) rSearchView.findViewById(R.id.to);

        ArrayAdapter adapter = (ArrayAdapter) auto_from.getAdapter();
        auto_from.setAdapter(null);
        auto_from.setText(sLocation);
        auto_from.setAdapter(adapter);

        auto_to.setAdapter(null);
        auto_to.setText(dLocation);
        auto_to.setAdapter(adapter);

        TextView mFrom = (TextView) getView().findViewById(R.id.from_location);
        mFrom.setText("From: " + sLocation);

        TextView mTo = (TextView) getView().findViewById(R.id.to_location);
        mTo.setText("To: " + dLocation);
    }

    public void enableEditMode(Boolean enable) {
        if (enable) {
            mMap.setOnMapLongClickListener(this);
            mMap.setOnMarkerClickListener(this);
            mMap.setOnMarkerDragListener(this);
        } else {
            mMap.setOnMapLongClickListener(null);
            mMap.setOnMarkerClickListener(null);
            mMap.setOnMarkerDragListener(null);
        }
    }

    public void saveRoute(){
        data route;
        if(editMode) {
            route = new data();
            String ePath = GoogleHelper.encodePath(rPolyline.getPoints());
            route.setRoute(sLocation, dLocation, ePath);
            route.setType(data.TYPE.savedRoute);
            HomeActivity.setCustomRoutes(route);
            Log.i(LOG_TAG, "set custom route");
        } else {
            if(!rPolyline.getPoints().isEmpty()) {
                route = HomeActivity.getCustomRoutes();
            } else {
                route = new data();
            }
            Log.i(LOG_TAG, "set tracked route");
            TrackerActivity activity = (TrackerActivity) getActivity();
            route.setTrackedRoute(GoogleHelper.encodePath(mPolyline.getPoints()));
            route.setDistance(activity.getCurrentDistance());
            //route.setAltitude(activity.getAltitudes());
            route.setSpeed(activity.getSpeeds());
            route.setType(data.TYPE.trackedRoute);
            HomeActivity.setTrackedRoutes(route);
        }
        HomeActivity.getCustomRoutes();
        HomeActivity.getTrackedRoutes();
    }
}