package com.dynamiqr.android.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class QrCode {

    @SerializedName("_id")
    private String id;

    @SerializedName("displayName")
    private String displayName;

    @SerializedName("qrType")
    private String qrType;

    @SerializedName("isActive")
    private boolean isActive;

    @SerializedName("linkMode")
    private String linkMode;

    @SerializedName("scanCount")
    private int scanCount;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("qrValue")
    private String qrValue;

    @SerializedName("qrInputs")
    private Map<String, Object> qrInputs;

    @SerializedName("style")
    private QrStyle style;

    @SerializedName("publicSlug")
    private String publicSlug;

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getQrType() {
        return qrType;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getLinkMode() {
        return linkMode;
    }

    public int getScanCount() {
        return scanCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getQrValue() {
        return qrValue;
    }

    public Map<String, Object> getQrInputs() {
        return qrInputs;
    }

    public QrStyle getStyle() {
        return style;
    }

    public String getPublicSlug() {
        return publicSlug;
    }
}
