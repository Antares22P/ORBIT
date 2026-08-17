package com.orbit.model;

public class Destination {

    private String name;
    private double latitude;
    private double longitude;

    public Destination() {
        // Required by Firebase
    }

    public Destination(
            String name,
            double latitude,
            double longitude) {

        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}