package com.example.myapplication.data.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private String id;
    
    @SerializedName("_id")
    private String mongoId;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("username")
    private String username;

    public String getId() { return id != null ? id : mongoId; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
}
