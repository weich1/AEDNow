package com.miscrew.aednow;

import android.widget.ImageView;

import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class InfoWindowRefresher implements Callback {
    private Marker marker;
    private String url;
    private ImageView userImage;

    public InfoWindowRefresher(Marker marker, String url, ImageView userImage) {
        this.marker = marker;
        this.url = url;
        this.userImage = userImage;
    }

    @Override
    public void onSuccess() {
        if (marker != null && marker.isInfoWindowShown()) {
            marker.hideInfoWindow();
            //Picasso.get()
            //        .load(url)
            //        .into(userImage);
            marker.showInfoWindow();
        }
    }

    @Override
    public void onError(Exception e) {

    }
}
