package com.example.myapplication.core.utils;

import java.util.HashMap;
import java.util.Map;

public final class QrEncoder {

    private QrEncoder() {
    }

    public static String encode(String type, String rawInput) {
        if (rawInput == null) {
            return "";
        }
        String value = rawInput.trim();
        if (value.isEmpty()) {
            return "";
        }
        String t = type != null ? type : "url";
        switch (t) {
            case "email":
                return value.startsWith("mailto:") ? value : "mailto:" + value;
            case "phone":
                return value.startsWith("tel:") ? value : "tel:" + value;
            case "wifi":
                return value;
            case "whatsapp": {
                String digits = value.replaceAll("\\D", "");
                return digits.isEmpty() ? "" : "https://wa.me/" + digits;
            }
            case "url":
            case "pdf":
            default:
                return value;
        }
    }

    public static Map<String, Object> buildQrInputs(String type, String rawInput) {
        Map<String, Object> inputs = new HashMap<>();
        String t = type != null ? type : "url";
        switch (t) {
            case "email":
                inputs.put("email", rawInput);
                break;
            case "phone":
                inputs.put("phone", rawInput);
                break;
            case "whatsapp":
                Map<String, String> wa = new HashMap<>();
                wa.put("phone", rawInput.replaceAll("\\D", ""));
                wa.put("message", "");
                inputs.put("whatsapp", wa);
                break;
            case "wifi":
                inputs.put("wifi", rawInput);
                break;
            default:
                inputs.put("url", rawInput);
                break;
        }
        return inputs;
    }
}
