package com.example.myapplication.core.utils;

public final class QrStyleMapper {

    private static final String[] BODY_SHAPES = {
            "square", "dots", "rounded", "extra-rounded", "classy", "classy-rounded"
    };

    private static final String[] CORNER_SHAPES = {
            "square", "dot", "rounded", "extra-rounded", "classy", "classy-rounded"
    };

    private QrStyleMapper() {
    }

    public static String bodyShapeFromIndex(int index) {
        if (index < 0 || index >= BODY_SHAPES.length) {
            return "square";
        }
        return BODY_SHAPES[index];
    }

    public static String bodyShapeFromId(String id) {
        if (id == null || !id.startsWith("body_")) {
            return "square";
        }
        try {
            int idx = Integer.parseInt(id.replace("body_", "")) - 1;
            return bodyShapeFromIndex(idx);
        } catch (NumberFormatException e) {
            return "square";
        }
    }

    public static String cornerShapeFromId(String id) {
        if (id == null || !id.startsWith("corner_")) {
            return "square";
        }
        try {
            int idx = Integer.parseInt(id.replace("corner_", "")) - 1;
            if (idx < 0 || idx >= CORNER_SHAPES.length) {
                return "square";
            }
            return CORNER_SHAPES[idx];
        } catch (NumberFormatException e) {
            return "square";
        }
    }
}
