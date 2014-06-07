package com.example.trackr;

import java.util.List;

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
}
