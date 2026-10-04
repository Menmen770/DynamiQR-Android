package com.dynamiqr.android.data.models;

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

    /** Israeli ID — course-local field (Room), not from Mongo. */
    private String idNumber;

    private String phone;

    /** ISO date yyyy-MM-dd — course-local field. */
    private String birthDate;

    /** Row id in local Room course_users table (not Mongo). */
    private long localRoomId;

    public String getId() {
        return id != null ? id : mongoId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public boolean hasPassword() {
        return hasPassword;
    }

    public void setHasPassword(boolean hasPassword) {
        this.hasPassword = hasPassword;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public long getLocalRoomId() {
        return localRoomId;
    }

    public void setLocalRoomId(long localRoomId) {
        this.localRoomId = localRoomId;
    }

    public long getLocalRowId() {
        return localRoomId > 0 ? localRoomId : -1L;
    }

    public String getDisplayName() {
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName.trim();
        }
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf('@'));
        }
        return "User";
    }

    public String getInitial() {
        String name = getDisplayName();
        return name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
    }
}
