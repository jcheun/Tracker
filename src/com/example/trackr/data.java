package com.example.trackr;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class data {
    public double avgSpeed = 0;
    public double maxSpeed = 0;
    public double avgAltitude = 0;
    public double maxAltitude = 0;
    public double minAltitude = 0;
    public double distance = 0;
    public int time = 0;
    public String date;
    public List<Double> speeds;
    public List<Double> altitudes;

    public String start = "";
    public String destination = "";
    public String route = "";


    public String trackedRoute = "";

    public TYPE type;

    public String sDate = "";
    public String sDuration = "";
    public String sDistance = "";
    public String sMaxSpeed = "";
    public String sAvgSpeed = "";
    public String sStart = "";
    public String sDestination = "";
    public String sRoute = "";
    public String sTrackedRoute = "";
    
    
    public static enum TYPE {
        savedRoute, trackedRoute
    }

    public void setRoute(String sLoc, String dLoc, String mRoute) {
        start = sLoc;
        destination = dLoc;
        route = mRoute;
    }

    public void setTrackedRoute(String tRoute) {
        trackedRoute = tRoute;
    }

    public void setSpeed(List<Double> rSpeeds) {
        speeds = rSpeeds;
        for(double speed : rSpeeds) {
            if(speed > maxSpeed) maxSpeed = speed;
            avgSpeed += speed;
        }
        avgSpeed /= rSpeeds.size();
    }

    public void setAltitude(List<Double> rAltitudes) {
        altitudes = rAltitudes;
        maxAltitude = minAltitude = rAltitudes.get(0);
        for(double altitude : rAltitudes) {
            if(altitude > maxAltitude) maxAltitude = altitude;
            if(altitude < minAltitude) minAltitude = altitude;
            avgAltitude += altitude;
        }
        avgAltitude /= rAltitudes.size();
    }

    public void setDistance(double tDistance) {
        distance = tDistance;
    }

    public void setTime(int tTime) {
        time = tTime;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public void setData(String date){
    	this.date = date;
    }
    
    public void setServerData(JSONObject data){
    	try {
    		Log.d("setting Data", data.toString());
			this.sAvgSpeed = data.getString("avg_speed");
			this.sDate = data.getString("date");
			this.sDestination = data.getString("destination");
			this.sDuration = data.getString("duration");
			this.sMaxSpeed = data.getString("max_speed");
			this.sDistance = data.getString("distance");
			this.sStart = data.getString("start");
			this.sTrackedRoute = data.getString("tracked_route");
			this.sRoute = data.getString("route");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
