package com.miscrew.aednow;

import com.google.android.gms.maps.model.Marker;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Mapper {
       @SerializedName("MapData")
       @Expose
       public ArrayList<MapData> mapData;
}
