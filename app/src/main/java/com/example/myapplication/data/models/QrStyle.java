package com.example.myapplication.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class QrStyle {

    @SerializedName("fgColor")
    private String fgColor;

    @SerializedName("bgColor")
    private String bgColor;

    @SerializedName("qrColorMode")
    private String qrColorMode;

    @SerializedName("dotsGradient")
    private Map<String, Object> dotsGradient;

    @SerializedName("bgColorMode")
    private String bgColorMode;

    @SerializedName("dotsType")
    private String dotsType;

    @SerializedName("cornersType")
    private String cornersType;

    @SerializedName("logoUrl")
    private String logoUrl;

    @SerializedName("logoShape")
    private String logoShape;

    @SerializedName("logoInsetScale")
    private Double logoInsetScale;

    @SerializedName("stickerType")
    private String stickerType;

    @SerializedName("errorCorrectionLevel")
    private String errorCorrectionLevel;

    public String getFgColor() {
        return fgColor;
    }

    public String getBgColor() {
        return bgColor;
    }

    public String getQrColorMode() {
        return qrColorMode;
    }

    public Map<String, Object> getDotsGradient() {
        return dotsGradient;
    }

    public String getBgColorMode() {
        return bgColorMode;
    }

    public String getDotsType() {
        return dotsType;
    }

    public String getCornersType() {
        return cornersType;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getLogoShape() {
        return logoShape;
    }

    public double getLogoInsetScale() {
        return logoInsetScale != null ? logoInsetScale : 1.0;
    }

    public String getStickerType() {
        return stickerType;
    }

    public String getErrorCorrectionLevel() {
        return errorCorrectionLevel;
    }
}
