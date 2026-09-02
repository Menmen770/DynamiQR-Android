package com.example.myapplication.core.utils;

import java.util.Arrays;
import java.util.List;

public class ColorProvider {
    public static List<String> getFgColors() {
        return Arrays.asList(
            "#111111", "#1f2937", "#4b5563", "#1877f2", "#1d4ed8", 
            "#0a9396", "#25d366", "#166534", "#3f6212", "#7f1d1d", "#5b21b6"
        );
    }

    public static List<String> getBgColors() {
        return Arrays.asList(
            "#ffffff", "#fde68a", "#fdba74", "#fca5a5", "#f9a8d4", 
            "#ddd6fe", "#bfdbfe", "#93c5fd", "#a7f3d0", "#86efac", "#d9f99d", "#e5e7eb"
        );
    }
}
