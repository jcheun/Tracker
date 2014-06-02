package com.example.trackr;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GoogleHelper {
    private static final String LOG_TAG = "GoogleHelper";

    public static String getJson(String url) {
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

        //Log.i(LOG_TAG, json.toString());
        return json.toString();
    }

    public static List<LatLng> parseJson(String jsonResults) {
        List<LatLng> cords = new ArrayList<LatLng>();
        try {
            // Parse Json string
            JSONObject object = new JSONObject(jsonResults);
            JSONArray routes = object.getJSONArray("routes");
            JSONObject route = routes.getJSONObject(0);

            JSONArray legs = route.getJSONArray("legs");
//            double distance = 0;
//            int sec = 0;
            for (int i = 0; i < legs.length(); ++i) {
                JSONObject leg = legs.getJSONObject(i);
                JSONArray steps = leg.getJSONArray("steps");
//                distance += Float.parseFloat(leg.getJSONObject("distance").getString("value"));
//                sec += Integer.parseInt(leg.getJSONObject("duration").getString("value"));
                for (int index = 0; index < steps.length(); ++index) {
                    JSONObject step = steps.getJSONObject(index);
                    JSONObject polyline = step.getJSONObject("polyline");
                    if (polyline.has("points")) {
                        cords.addAll(GoogleHelper.decodePath(polyline.getString("points")));
                    }
                }
            }

//            TextView dist = (TextView) getActivity().findViewById(R.id.DistTime);
//            dist.setText(String.format("Distance: %.2f mi\t Est Time: %d hour %d mins",
//                    distance * 0.00062137, sec / (60 * 60), sec/60 % 60));

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
            e.printStackTrace();
        }
        return cords;
    }
    public static List<LatLng> decodePath(String path) {
        List<Double> points = new ArrayList<Double>();
        List<LatLng> cord = new ArrayList<LatLng>();
        for(String code : path.split("(?<=[\\x00-\\x5E])")) {
            int point = 0;
            for(int index = 0; index < code.length(); ++index) {
                point += ((code.charAt(index) - 63) & 0x1F) << (index*5);
            }
            points.add((double)(((point & 0x01) == 1) ? ~(point >> 1): (point >> 1)) / 100000);
        }

        double latBase = 0, longBase = 0;
        for(int index = 0; index < points.size(); ++index) {
            cord.add(new LatLng(latBase += points.get(index), longBase += points.get(++index)));
        }
        return cord;
    }

    public static String encodePath(List<LatLng> points) {
        double latPrev = 0, longPrev = 0;
        StringBuilder ePath = new StringBuilder();
        for(LatLng cord : points) {
            ePath.append(encodePoint((int)Math.round((cord.latitude - latPrev) * 100000)));
            ePath.append(encodePoint((int)Math.round((cord.longitude - longPrev) * 100000)));
            latPrev = cord.latitude;
            longPrev = cord.longitude;
        }
        return ePath.toString();
    }

    public static String encodePoint(int point) {
        if(point == 0) return "?";
        StringBuilder sPoint = new StringBuilder();
        point = (point < 0) ? ~(point << 1) : point << 1;
        for (; point != 0; point >>= 5) {
            if (point <= 0x1F) {
                sPoint.append(Character.toChars((point & 0x1F) + 63));
            } else {
                sPoint.append(Character.toChars((point & 0x1F | 0x20) + 63));
            }
        }
        return sPoint.toString();
    }

    public static void moveCamera(LatLng cords, float bearing, float zoom, GoogleMap mMap) {
        CameraPosition myPosition = new CameraPosition.Builder()
                .target(cords)
                .zoom(zoom)
                .bearing(bearing)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(myPosition), null);
    }

    public static String geoToAdress(LatLng latLng, Geocoder mGeocoder) {
        String addressText = null;
        try {
            List<Address> mAddress = mGeocoder.getFromLocation(latLng.latitude,
                                                               latLng.longitude, 1);
            Address address = mAddress.get(0);
            addressText = String.format("%s, %s, %s",
                    // If there's a street address, add it
                    address.getMaxAddressLineIndex() > 0 ?
                            address.getAddressLine(0) : "",
                    // Locality is usually a city
                    address.getLocality(),
                    // The country of the address
                    address.getCountryName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return addressText;
    }

}
