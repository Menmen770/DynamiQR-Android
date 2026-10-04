package com.dynamiqr.android.core.utils;

/** Israeli national ID (ת.ז.) checksum validation. */
public final class IsraeliIdValidator {

    private IsraeliIdValidator() {
    }

    public static boolean isValid(String raw) {
        if (raw == null) {
            return false;
        }
        String id = raw.trim().replace("-", "").replace(" ", "");
        if (!id.matches("\\d{5,9}")) {
            return false;
        }
        while (id.length() < 9) {
            id = "0" + id;
        }
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            int digit = id.charAt(i) - '0';
            int stepped = digit * ((i % 2) + 1);
            sum += stepped > 9 ? stepped - 9 : stepped;
        }
        return sum % 10 == 0;
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String id = raw.trim().replace("-", "").replace(" ", "");
        while (id.length() < 9 && id.matches("\\d+")) {
            id = "0" + id;
        }
        return id;
    }
}
