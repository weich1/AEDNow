package com.miscrew.aednow;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
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
        TextView title = myContentsView.findViewById(R.id.title);
        TextView snippet = myContentsView.findViewById(R.id.snippet);
        ViewGroup layoutImages = (ViewGroup) myContentsView.findViewById(R.id.layoutImages);
        for(MapData x: markerSet.mapData) {
            if (x.getMarker().equals(marker.getId())) {
                title.setText(x.getTitle());
                snippet.setText(x.getDescription());
                for(String imgUrl: x.getImages()) {
                    ImageView imgView = new ImageView(myContentsView.getContext());
                    imgView.setLayoutParams(new FrameLayout.LayoutParams(150, 150, Gravity.CENTER));
                    imgView.setTextAlignment(FrameLayout.TEXT_ALIGNMENT_CENTER);
                    imgView.setEnabled(true);
                    loadImage(imgView, imgUrl);
                    layoutImages.addView(imgView);
                    Picasso.get()
                            .load(imgUrl)
                            .placeholder(R.mipmap.ic_launcher)
                            .into(imgView, new InfoWindowRefresher(marker, imgUrl, imgView));
                }
            }
        }
            return myContentsView;
    }

    private void loadImage(ImageView img, String url) {
        if(url.length() == 0) return;
        Picasso.get()
                .load(url)
                .placeholder(R.mipmap.ic_launcher)
                .into(img);
    }

    @Override
    public void onInfoWindowLongClick(@NonNull Marker marker) {
        for(MapData x: markerSet.mapData) {
            if (x.getMarker().equals(marker.getId())) {
                try {
                    Intent mIntent = new Intent(context, InfoExpandActivity.class);

                    //Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().excludeFieldsWithModifiers(Modifier.PUBLIC).create();
                    Gson gson = new Gson();
                    mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.putExtra("mapdata", gson.toJson(x)); // package up map marker data into intent
                   context.startActivity(mIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}