package com.dynamiqr.android.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QrListResponse {
    @SerializedName("items")
    private List<QrCode> items;

    public List<QrCode> getItems() { return items; }
}
