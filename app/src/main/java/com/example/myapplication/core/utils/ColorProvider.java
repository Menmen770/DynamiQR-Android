package com.example.myapplication.core.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    /** כמו PRESET_QR_COLORS ב-RN */
    public static List<String> getFgColors() {
        return Arrays.asList(
                "#111111", "#1f2937", "#4b5563", "#1877f2", "#1d4ed8",
                "#0a9396", "#25d366", "#166534", "#3f6212", "#7f1d1d", "#5b21b6"
        );
    }

    /** כמו PRESET_BG_COLORS ב-RN (+ לבן לנוחות) */
    public static List<String> getBgColors() {
        return Arrays.asList(
                "#ffffff", "#fde68a", "#fdba74", "#fca5a5", "#f9a8d4",
                "#ddd6fe", "#bfdbfe", "#93c5fd", "#a7f3d0", "#86efac", "#d9f99d", "#e5e7eb"
        );
    }

    /** פלטה מורחבת לדיאלוג העיפרון (בלי הקלדת HEX) */
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

    /** כמו QR_GRADIENT_PRESETS ב-RN */
    public static List<GradientPreset> getQrGradientPresets() {
        return Arrays.asList(
                new GradientPreset("brand-teal", "טורקיז מותג", "#0a9396", "#005f73", 135),
                new GradientPreset("social-pop", "פופ חברתי", "#7c3aed", "#ec4899", 135),
                new GradientPreset("sunrise", "זריחה", "#f97316", "#ef4444", 135),
                new GradientPreset("electric", "חשמלי", "#2563eb", "#06b6d4", 120),
                new GradientPreset("lime-night", "ליים כהה", "#65a30d", "#14532d", 135),
                new GradientPreset("royal", "מלכותי", "#4338ca", "#7c3aed", 140),
                new GradientPreset("ruby-fire", "אש אדומה", "#dc2626", "#fb7185", 135),
                new GradientPreset("matrix", "מטריקס", "#22c55e", "#15803d", 135),
                new GradientPreset("arctic", "ארקטי", "#38bdf8", "#6366f1", 140),
                new GradientPreset("gold-plum", "זהב־שזיף", "#f59e0b", "#7c3aed", 135)
        );
    }

    public static List<GradientPreset> getBgGradientPresets() {
        return Arrays.asList(
                new GradientPreset("peach-cream", "אפרסק", "#fff7ed", "#fdba74", 135),
                new GradientPreset("sky-mint", "שמיים־מנטה", "#dbeafe", "#a7f3d0", 135),
                new GradientPreset("lavender-blush", "לבנדר", "#ede9fe", "#fbcfe8", 135),
                new GradientPreset("sunset-soft", "שקיעה רכה", "#fde68a", "#fca5a5", 135),
                new GradientPreset("ocean-silk", "אוקיינוס", "#bfdbfe", "#93c5fd", 135),
                new GradientPreset("stone-glow", "אבן", "#f5f5f4", "#d6d3d1", 140),
                new GradientPreset("citrus-fresh", "הדרים", "#fef08a", "#86efac", 135),
                new GradientPreset("rose-cloud", "ענן ורוד", "#ffe4e6", "#fbcfe8", 135)
        );
    }

    public static List<Integer> getGradientAngles() {
        return Collections.unmodifiableList(Arrays.asList(0, 45, 90, 135, 180, 225, 270, 315));
    }
}
