package com.example.myapplication.data.models;

import com.google.gson.annotations.SerializedName;

public class QrCode {
    @SerializedName("_id")
    private String id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("isActive")
    private boolean isActive;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public boolean isActive() { return isActive; }
}
