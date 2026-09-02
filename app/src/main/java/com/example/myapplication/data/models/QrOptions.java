package com.example.myapplication.data.models;

public class QrOptions {
    public enum BodyShape { SQUARE, DOTS, ROUNDED, EXTRA_ROUNDED, CLASSY, CLASSY_ROUNDED }
    public enum EyeShape { SQUARE, DOT, ROUNDED, EXTRA_ROUNDED, CLASSY, CLASSY_ROUNDED }
    public enum LogoShape { NONE, SQUARE, CIRCLE, OVERLAY }

    private String content = "";
    private String fgColor = "#0A9396";
    private String bgColor = "#FFFFFF";
    private BodyShape bodyShape = BodyShape.SQUARE;
    private EyeShape eyeShape = EyeShape.SQUARE;
    private LogoShape logoShape = LogoShape.NONE;
    private String logoUrl = null;
    private String stickerId = "none";
    private String qrColorMode = "solid";
    private String[] gradientColors = {"#0A9396", "#005F73"};

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getFgColor() { return fgColor; }
    public void setFgColor(String fgColor) { this.fgColor = fgColor; }
    public String getBgColor() { return bgColor; }
    public void setBgColor(String bgColor) { this.bgColor = bgColor; }
    public BodyShape getBodyShape() { return bodyShape; }
    public void setBodyShape(BodyShape bodyShape) { this.bodyShape = bodyShape; }
    public EyeShape getEyeShape() { return eyeShape; }
    public void setEyeShape(EyeShape eyeShape) { this.eyeShape = eyeShape; }
    public LogoShape getLogoShape() { return logoShape; }
    public void setLogoShape(LogoShape logoShape) { this.logoShape = logoShape; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getStickerId() { return stickerId; }
    public void setStickerId(String stickerId) { this.stickerId = stickerId; }
    public String getQrColorMode() { return qrColorMode; }
    public void setQrColorMode(String qrColorMode) { this.qrColorMode = qrColorMode; }
    public String[] getGradientColors() { return gradientColors; }
    public void setGradientColors(String[] colors) { this.gradientColors = colors; }
}
