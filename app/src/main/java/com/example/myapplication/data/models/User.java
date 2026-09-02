package com.example.myapplication.data.models;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("id")
    private String id;

    @SerializedName("_id")
    private String mongoId;

    @SerializedName("email")
    private String email;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("username")
    private String username;

    @SerializedName("hasPassword")
    private boolean hasPassword;

    public String getId() {
        return id != null ? id : mongoId;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public boolean hasPassword() {
        return hasPassword;
    }

    public String getDisplayName() {
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName.trim();
        }
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf('@'));
        }
        return "משתמש";
    }

    public String getInitial() {
        String name = getDisplayName();
        return name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
    }
}
