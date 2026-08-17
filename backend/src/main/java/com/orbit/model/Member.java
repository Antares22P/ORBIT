package com.orbit.model;

import com.google.firebase.database.PropertyName;

public class Member {

    private String id;
    private String name;
    private String avatar;
    private String color;

    private double latitude;
    private double longitude;

    private double accuracy;
    private double speed;
    private double heading;

    private long updatedAt;


    public Member() {
        // Required by Firebase
    }


    public Member(
            String id,
            String name,
            String avatar,
            String color) {

        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.color = color;

        this.latitude = 0;
        this.longitude = 0;
        this.accuracy = 0;
        this.speed = 0;
        this.heading = 0;

        this.updatedAt =
                System.currentTimeMillis();
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }


    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }


    @PropertyName("lat")
    public double getLatitude() {
        return latitude;
    }

    @PropertyName("lat")
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


    @PropertyName("lng")
    public double getLongitude() {
        return longitude;
    }

    @PropertyName("lng")
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }


    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }


    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }


    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }


    public void updateLocation(
            double latitude,
            double longitude,
            double accuracy,
            double speed,
            double heading) {

        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.speed = speed;
        this.heading = heading;

        this.updatedAt =
                System.currentTimeMillis();
    }
}