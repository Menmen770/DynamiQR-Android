package com.example.myapplication.ui.generator;

public class QrType {
    private String id;
    private String label;
    private int iconRes;

    public QrType(String id, String label, int iconRes) {
        this.id = id;
        this.label = label;
        this.iconRes = iconRes;
    }

    public String getId() { return id; }
    public String getLabel() { return label; }
    public int getIconRes() { return iconRes; }
}
