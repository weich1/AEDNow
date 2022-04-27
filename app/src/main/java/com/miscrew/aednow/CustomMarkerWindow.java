package com.miscrew.aednow;

import android.content.Context;
import android.content.Intent;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Modifier;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class CustomMarkerWindow extends AppCompatActivity implements GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowLongClickListener {
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
            if (x.getMarker().equals(marker.getId())) {
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
    public void onInfoWindowLongClick(@NonNull Marker marker) {
        for(MapData x: markerSet.mapData) {
            if (x.getMarker().equals(marker.getId())) {
                try {
                    Intent mIntent = new Intent(context, InfoExpandActivity.class);

                    //Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().excludeFieldsWithModifiers(Modifier.PUBLIC).create();
                    Gson gson = new Gson();
                    mIntent.putExtra("mapdata", gson.toJson(x)); // package up map marker data into intent
                   context.startActivity(mIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //if(MapsActivity.isLoggedIn()) {
                    /*Intent mIntent = new Intent(this, AddActivity.class);
                    mIntent.putExtra("lat", latLng.latitude);
                    mIntent.putExtra("lng", latLng.longitude);
                    startActivity(mIntent);*/
                //}
            }
        }
    }


}