package com.miscrew.aednow;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class MapData {
    @SerializedName("title") // added just for sake of reference
    @Expose
    private String title;
    private double lat;
    private double lng;
    @SerializedName("description") // added just for sake of reference
    @Expose
    private String description;

    public MapData(String title, double lat, double lng, String description) {
        this.title = title;
        this.lat = lat;
        this.lng = lng;
        this.description = description;
    }

    // setters and getters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public void getLat(long lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void getLng(long lng) {
        this.lng = lng;
    }
}
