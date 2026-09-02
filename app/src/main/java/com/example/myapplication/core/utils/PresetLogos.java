package com.example.myapplication.core.utils;

import android.content.Context;
import com.example.myapplication.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PresetLogos {

    public static final class Preset {
        public final String id;
        public final int rawResId;
        public final float rasterInset;

        Preset(String id, int rawResId, float rasterInset) {
            this.id = id;
            this.rawResId = rawResId;
            this.rasterInset = rasterInset;
        }
    }

    private static final Map<String, Preset> BY_ID = new HashMap<>();

    static {
        register("whatsapp", R.raw.whatsapp, 1f);
        register("instagram", R.raw.instagram, 1f);
        register("facebook", R.raw.facebook, 1f);
        register("linkedin", R.raw.linkedin, 1f);
        register("telegram", R.raw.telegram, 1f);
        register("spotify", R.raw.spotify, 1f);
        register("google", R.raw.google, 0.88f);
        register("github", R.raw.github, 1f);
        register("youtube", R.raw.youtube, 0.82f);
        register("tiktok", R.raw.tiktok, 1f);
        register("x", R.raw.x, 0.46f);
        register("bit", R.raw.bit, 0.46f);
        register("google_maps", R.raw.google_maps, 0.9f);
        register("waze", R.raw.waze, 0.9f);
    }

    private PresetLogos() {
    }

    private static void register(String id, int rawResId, float inset) {
        BY_ID.put(id, new Preset(id, rawResId, inset));
    }

    public static List<Preset> all() {
        return new ArrayList<>(BY_ID.values());
    }

    public static Preset find(String id) {
        return BY_ID.get(id);
    }

    public static int rawResForId(Context context, String id) {
        Preset preset = BY_ID.get(id);
        if (preset != null) {
            return preset.rawResId;
        }
        return context.getResources().getIdentifier(id, "raw", context.getPackageName());
    }

    public static float insetForId(String id) {
        Preset preset = BY_ID.get(id);
        return preset != null ? preset.rasterInset : 1f;
    }

    public static Map<String, Preset> asMap() {
        return Collections.unmodifiableMap(BY_ID);
    }
}
