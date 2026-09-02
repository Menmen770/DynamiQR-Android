package com.example.myapplication.data.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("token")
    private String token;
    
    @SerializedName("error")
    private String error;
    
    @SerializedName("user")
    private User user;

    public String getToken() { return token; }
    public String getError() { return error; }
    public User getUser() { return user; }
}
