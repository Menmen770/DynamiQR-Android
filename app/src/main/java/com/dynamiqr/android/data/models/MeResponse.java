package com.dynamiqr.android.data.models;

import com.google.gson.annotations.SerializedName;

public class MeResponse {

    @SerializedName("user")
    private User user;

    public User getUser() {
        return user;
    }
}
