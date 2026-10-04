package com.example.myapplication.core.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Simple SHA-256 hash for the temporary course-local user store. */
public final class PasswordHasher {

    private PasswordHasher() {
    }

    public static String hash(String password) {
        if (password == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public static boolean matches(String rawPassword, String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        return storedHash.equals(hash(rawPassword));
    }
}
