package com.example.myapplication.core.utils;

import com.example.myapplication.ui.adapters.StyleThumbnailAdapter;
import java.util.ArrayList;
import java.util.List;

public class PresetData {
    public static class StyleItemImpl implements StyleThumbnailAdapter.StyleItem {
        private String id;
        private int imageResId;
        private boolean isSvg;

        public StyleItemImpl(String id, int imageResId, boolean isSvg) {
            this.id = id;
            this.imageResId = imageResId;
            this.isSvg = isSvg;
        }

        @Override public String getId() { return id; }
        @Override public int getImageResId() { return imageResId; }
        @Override public boolean isSvg() { return isSvg; }
    }
}
