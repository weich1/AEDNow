package com.miscrew.aednow;

import com.google.android.gms.maps.model.Marker;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class MapData {

    private String marker;
    private String title;
    private String[] images;
    private double lat;
    private double lng;
    private int votes;
    private int icon;
    //@SerializedName("description")
    //@Expose
    private String description;

    public MapData(String title, double lat, double lng, int icon, String description, String markerId) {
        this.title = title;
        this.lat = lat;
        this.lng = lng;
        this.icon = icon;
        this.description = description;
        this.marker = markerId;
    }

    public MapData(String title, double lat, double lng, int icon, String description) {
        this.title = title;
        this.lat = lat;
        this.lng = lng;
        this.icon = icon;
        this.description = description;
        this.marker = null;
    }



    // setters and getters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getImages() {
        return images;
    }

    public void setImgUrl1(String []images) {
        this.images = images;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(long lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(long lng) {
        this.lng = lng;
    }

    public int getIcon() {return icon; }

    public void setIcon(int icon) {this.icon = icon; }

    public int getVotes() { return votes; }

    public void setVotes(int votes) { this.votes = votes; }

    public String getMarker() { return this.marker; }

    public void setMarker(String marker) { this.marker = marker; }

}
