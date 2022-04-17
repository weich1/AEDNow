package com.miscrew.aednow;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

public class CustomMarkerWindow implements GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {
    private Mapper markerSet;
    private Context context;
    private View myContentsView;
    private ViewGroup mcv2;

    public CustomMarkerWindow(Context context, Mapper markerSet) {
        this.context = context;
        this.markerSet = markerSet;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        myContentsView = inflater.inflate(R.layout.map_info_content, null);
        ImageView image1 = (ImageView) myContentsView.findViewById(R.id.img1);
        TextView title = (TextView) myContentsView.findViewById(R.id.title);
        TextView snippet = (TextView) myContentsView.findViewById(R.id.snippet);

        for(MapData x: markerSet.mapData) {
            if (x.getMarker().getId().equals(marker.getId())) {
                title.setText(x.getTitle());
                snippet.setText(x.getDescription());
                if (x.imagesLoaded) {
                    Picasso.get()
                            .load(x.getImgUrl1())
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(image1);
                } else {
                    x.imagesLoaded = true;
                    Picasso.get()
                            .load(x.getImgUrl1())
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(image1, new InfoWindowRefresher(marker));
                }
            }
        }
            return myContentsView;
    }


    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        for(MapData x: markerSet.mapData) {
            if (x.getMarker().getId().equals(marker.getId())) {
                System.out.println("Text");
            }
        }
    }
}