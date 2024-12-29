package com.example.real_timepositioningrescuesystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class CustomMarker implements ClusterItem {
    private LatLng position;
    private String title;
    private String snippet;
    private int icon;
    private User user;
    public CustomMarker(LatLng position, String title, String snippet, int icon, User user) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.icon = icon;
        this.user = user;
    }
    public CustomMarker(){

    }
    @NonNull
    @Override
    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return snippet;
    }
    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Nullable
    @Override
    public Float getZIndex() {
        return 0f;
    }
}
