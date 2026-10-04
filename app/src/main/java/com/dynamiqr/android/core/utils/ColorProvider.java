package com.dynamiqr.android.core.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Color / gradient presets aligned with RN {@code PRESET_*} and {@code QR_GRADIENT_PRESETS}.
 * Display names are English keys; localize via strings.xml when shown.
 */
public final class ColorProvider {

    public static final class GradientPreset {
        public final String id;
        public final String name;
        public final String start;
        public final String end;
        public final int angle;

        public GradientPreset(String id, String name, String start, String end, int angle) {
            this.id = id;
            this.name = name;
            this.start = start;
            this.end = end;
            this.angle = angle;
        }
    }

    private ColorProvider() {
    }

    public static List<String> getFgColors() {
        return Arrays.asList(
                "#111111", "#1f2937", "#4b5563", "#1877f2", "#1d4ed8",
                "#0a9396", "#25d366", "#166534", "#3f6212", "#7f1d1d", "#5b21b6"
        );
    }

    public static List<String> getBgColors() {
        return Arrays.asList(
                "#ffffff", "#fde68a", "#fdba74", "#fca5a5", "#f9a8d4",
                "#ddd6fe", "#bfdbfe", "#93c5fd", "#a7f3d0", "#86efac", "#d9f99d"
        );
    }

    public static List<String> getExtendedFgColors() {
        return Arrays.asList(
                "#111111", "#1f2937", "#374151", "#4b5563", "#6b7280",
                "#1877f2", "#1d4ed8", "#2563eb", "#0ea5e9", "#0891b2",
                "#0a9396", "#14b8a6", "#25d366", "#166534", "#15803d",
                "#3f6212", "#65a30d", "#ca8a04", "#ea580c", "#dc2626",
                "#7f1d1d", "#be123c", "#db2777", "#5b21b6", "#7c3aed"
        );
    }

    public static List<String> getExtendedBgColors() {
        return Arrays.asList(
                "#ffffff", "#f8fafc", "#f1f5f9", "#e5e7eb", "#fde68a",
                "#fef08a", "#fdba74", "#fed7aa", "#fca5a5", "#fecaca",
                "#f9a8d4", "#fbcfe8", "#ddd6fe", "#ede9fe", "#bfdbfe",
                "#dbeafe", "#93c5fd", "#a7f3d0", "#86efac", "#d9f99d",
                "#bbf7d0", "#f5d0a9", "#ffe4e6", "#fafaf9"
        );
    }

    public static List<GradientPreset> getQrGradientPresets() {
        return Arrays.asList(
                new GradientPreset("brand-teal", "Deep teal", "#0a9396", "#005f73", 135),
                new GradientPreset("social-pop", "Pink purple", "#7c3aed", "#ec4899", 135),
                new GradientPreset("sunrise", "Warm sunset", "#f97316", "#ef4444", 135),
                new GradientPreset("electric", "Electric blue", "#2563eb", "#06b6d4", 120),
                new GradientPreset("lime-night", "Dark lime", "#65a30d", "#14532d", 135),
                new GradientPreset("royal", "Royal purple", "#4338ca", "#7c3aed", 140),
                new GradientPreset("ruby-fire", "Hot ruby", "#dc2626", "#fb7185", 135),
                new GradientPreset("matrix", "Neon green", "#22c55e", "#15803d", 135),
                new GradientPreset("arctic", "Ice blue", "#38bdf8", "#6366f1", 140),
                new GradientPreset("gold-plum", "Gold plum", "#f59e0b", "#7c3aed", 135)
        );
    }

    public static List<GradientPreset> getBgGradientPresets() {
        return Arrays.asList(
                new GradientPreset("peach-cream", "Peach", "#fff7ed", "#fdba74", 135),
                new GradientPreset("sky-mint", "Sky mint", "#dbeafe", "#a7f3d0", 135),
                new GradientPreset("lavender-blush", "Lavender", "#ede9fe", "#fbcfe8", 135),
                new GradientPreset("sunset-soft", "Soft sunset", "#fde68a", "#fca5a5", 135),
                new GradientPreset("ocean-silk", "Ocean", "#bfdbfe", "#93c5fd", 135),
                new GradientPreset("stone-glow", "Stone", "#f5f5f4", "#d6d3d1", 140),
                new GradientPreset("citrus-fresh", "Citrus", "#fef08a", "#86efac", 135),
                new GradientPreset("rose-cloud", "Rose cloud", "#ffe4e6", "#fbcfe8", 135)
        );
    }

    public static List<Integer> getGradientAngles() {
        return Collections.unmodifiableList(Arrays.asList(0, 45, 90, 135, 180, 225, 270, 315));
    }
}
