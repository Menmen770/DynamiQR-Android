package com.dynamiqr.android.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import com.dynamiqr.android.data.models.User;
import com.google.gson.Gson;

/**
 * Session holder. Real JWT comes from the existing backend (QR APIs).
 * Course profile extras (ת.ז. / phone / birth) live in Room and are merged into the cached User.
 */
public class AuthManager {

    private static final String PREF_NAME = "dynamiqr_auth";
    private static final String KEY_TOKEN = "dynamiqrAuthJwt";
    private static final String KEY_USER = "dynamiqrAuthUser";
    private static final String KEY_LOCAL_USER_ID = "localCourseUserId";
    /** Marker token so existing isLoggedIn checks work without a real backend JWT. */
    public static final String LOCAL_SESSION_TOKEN = "local-course-session";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public AuthManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void saveUser(User user) {
        if (user == null) {
            prefs.edit().remove(KEY_USER).remove(KEY_LOCAL_USER_ID).apply();
            return;
        }
        SharedPreferences.Editor editor = prefs.edit().putString(KEY_USER, gson.toJson(user));
        long localId = user.getLocalRowId();
        if (localId > 0) {
            editor.putLong(KEY_LOCAL_USER_ID, localId);
        }
        editor.apply();
    }

    public User getUser() {
        String json = prefs.getString(KEY_USER, null);
        if (json == null || json.isEmpty()) {
            return null;
        }
        return gson.fromJson(json, User.class);
    }

    public void saveSession(String token, User user) {
        SharedPreferences.Editor editor = prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER, gson.toJson(user));
        if (user != null && user.getLocalRowId() > 0) {
            editor.putLong(KEY_LOCAL_USER_ID, user.getLocalRowId());
        }
        editor.apply();
    }

    /** Starts a course-local session (Room user). No Mongo involvement. */
    public void saveLocalSession(User user) {
        prefs.edit()
                .putString(KEY_TOKEN, LOCAL_SESSION_TOKEN)
                .putString(KEY_USER, gson.toJson(user))
                .putLong(KEY_LOCAL_USER_ID, user.getLocalRowId())
                .apply();
    }

    public long getLocalUserId() {
        return prefs.getLong(KEY_LOCAL_USER_ID, -1L);
    }

    public boolean isLocalSession() {
        return LOCAL_SESSION_TOKEN.equals(getToken());
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.isEmpty();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
