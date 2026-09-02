package com.example.myapplication.data.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("token")
    private String token;

    @SerializedName("error")
    private String error;

    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private User user;

    @SerializedName("needsEmailVerification")
    private boolean needsEmailVerification;

    @SerializedName("email")
    private String email;

    public String getToken() {
        return token;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    public boolean isNeedsEmailVerification() {
        return needsEmailVerification;
    }

    public String getEmail() {
        return email;
    }
}
