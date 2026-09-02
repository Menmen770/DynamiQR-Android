package com.example.myapplication.data.models;

import com.google.gson.annotations.SerializedName;

public class GenerateQrResponse {

    @SerializedName("qrImage")
    private String qrImage;

    public String getQrImage() {
        return qrImage;
    }
}
