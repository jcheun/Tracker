package com.example.trackr;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
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


public class RouteSearchActivity extends Fragment {

    private static final String LOG_TAG = "RouteSearchActivity";
    private static final String API_KEY = "AIzaSyA8EWxqHaKOKqbR6Y7XrpdMYUVPB_POu48";


    private AutoCompleteTextView from;
    private AutoCompleteTextView to;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.actvity_search, container, false);

        from = (AutoCompleteTextView) view.findViewById(R.id.from);
        from.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line));

        to = (AutoCompleteTextView) view.findViewById(R.id.to);
        to.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line));

        Button btnRoute = (Button) view.findViewById(R.id.btnRoute);
        btnRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG_TAG, from.getText().toString());
                Log.i(LOG_TAG, to.getText().toString());
                GoogleMapActivity gMapFrag = (GoogleMapActivity) getFragmentManager().findFragmentByTag("android:switcher:"+R.id.pager+":"+1);
                gMapFrag.setAddress(from.getText().toString(), to.getText().toString());
                //gMapFrag.updateRoute();

                ViewPager vPager = (ViewPager) getActivity().findViewById(R.id.pager);
                vPager.setCurrentItem(1);
            }
        });

        return view;

    }

    private class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> resultList;

        public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }};
            return filter;
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

        //Log.i(LOG_TAG, json.toString());
        return json.toString();
    }

    private ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = null;

        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/place/autocomplete/json");
        url.append("?sensor=true&key=" + API_KEY);
        try {
            url.append("&input=" + URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            e.printStackTrace();
        }

        String jsonResults = getJson(url.toString());

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults);
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }
}