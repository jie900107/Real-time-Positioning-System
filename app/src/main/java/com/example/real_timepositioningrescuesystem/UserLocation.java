package com.example.real_timepositioningrescuesystem;

public class UserLocation {
    Double latitude;
    Double longitude;
    User user;
    public UserLocation(Double latitude, Double longitude, User user) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.user = user;
    }
    public UserLocation(){

    }
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "UserLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", user=" + user +
                '}';
    }
}
