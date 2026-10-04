package com.dynamiqr.android.core.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class QrEncoder {

    private QrEncoder() {
    }

    public static String encode(String type, String rawInput) {
        return encode(type, rawInput, null);
    }

    public static String encode(String type, String rawInput, String message) {
        if (rawInput == null) {
            return "";
        }
        String value = rawInput.trim();
        if (value.isEmpty()) {
            return "";
        }
        String msg = message != null ? message : "";
        String t = type != null ? type : "url";
        switch (t) {
            case "email":
                return value.startsWith("mailto:") ? value : "mailto:" + value;
            case "phone":
                return value.startsWith("tel:") ? value : "tel:" + value;
            case "sms": {
                String phone = value.replace(" ", "");
                if (msg.isEmpty()) {
                    return "sms:" + phone;
                }
                return "sms:" + phone + "?body=" + urlEncode(msg);
            }
            case "wifi":
                // Prefer structured encodeWifi(); raw path kept for already-built WIFI: strings.
                if (value.startsWith("WIFI:")) {
                    return value;
                }
                return encodeWifi(value, "", "WPA");
            case "whatsapp": {
                String digits = value.replaceAll("\\D", "");
                if (digits.isEmpty()) {
                    return "";
                }
                if (msg.isEmpty()) {
                    return "https://wa.me/" + digits;
                }
                return "https://wa.me/" + digits + "?text=" + urlEncode(msg);
            }
            case "url":
            case "pdf":
            default:
                return value;
        }
    }

    public static String encodeWifi(String ssid, String password, String security) {
        String network = ssid != null ? ssid.trim() : "";
        if (network.isEmpty()) {
            return "";
        }
        String sec = normalizeWifiSecurity(security);
        String pass = password != null ? password : "";
        if ("nopass".equals(sec)) {
            pass = "";
        }
        return "WIFI:T:" + sec
                + ";S:" + escapeWifi(network)
                + ";P:" + escapeWifi(pass)
                + ";;";
    }

    public static Map<String, Object> buildQrInputs(String type, String rawInput) {
        return buildQrInputs(type, rawInput, null);
    }

    public static Map<String, Object> buildQrInputs(String type, String rawInput, String message) {
        Map<String, Object> inputs = new HashMap<>();
        String t = type != null ? type : "url";
        String msg = message != null ? message : "";
        switch (t) {
            case "email":
                inputs.put("email", rawInput);
                break;
            case "phone":
                inputs.put("phone", rawInput);
                break;
            case "sms": {
                Map<String, String> sms = new HashMap<>();
                sms.put("phone", rawInput != null ? rawInput.trim() : "");
                sms.put("message", msg);
                inputs.put("sms", sms);
                break;
            }
            case "whatsapp": {
                Map<String, String> wa = new HashMap<>();
                wa.put("phone", rawInput != null ? rawInput.replaceAll("\\D", "") : "");
                wa.put("message", msg);
                inputs.put("whatsapp", wa);
                break;
            }
            case "wifi":
                inputs.put("wifi", rawInput);
                break;
            case "pdf":
                inputs.put("pdf", rawInput);
                break;
            default:
                inputs.put("url", rawInput);
                break;
        }
        return inputs;
    }

    public static Map<String, Object> buildWifiQrInputs(String ssid, String password, String security) {
        Map<String, Object> inputs = new HashMap<>();
        Map<String, String> wifi = new HashMap<>();
        wifi.put("ssid", ssid != null ? ssid.trim() : "");
        wifi.put("password", password != null ? password : "");
        wifi.put("security", normalizeWifiSecurity(security));
        inputs.put("wifi", wifi);
        return inputs;
    }

    public static String normalizeWifiSecurity(String security) {
        if (security == null) {
            return "WPA";
        }
        String s = security.trim();
        if ("WEP".equalsIgnoreCase(s)) {
            return "WEP";
        }
        if ("nopass".equalsIgnoreCase(s) || "open".equalsIgnoreCase(s) || "none".equalsIgnoreCase(s)) {
            return "nopass";
        }
        return "WPA";
    }

    private static String escapeWifi(String value) {
        return value
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\"", "\\\"");
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }
}
