package com.example.myapplication.data.models;

import com.google.gson.annotations.SerializedName;

public class SaveQrResponse {

    @SerializedName("saved")
    private QrCode saved;

    @SerializedName("updated")
    private boolean updated;

    @SerializedName("error")
    private String error;

    public QrCode getSaved() {
        return saved;
    }

    public boolean isUpdated() {
        return updated;
    }

    public String getError() {
        return error;
    }
}
