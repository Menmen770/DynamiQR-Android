package com.example.myapplication.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.myapplication.data.models.User;
import com.google.gson.Gson;

public class AuthManager {

    private static final String PREF_NAME = "dynamiqr_auth";
    private static final String KEY_TOKEN = "dynamiqrAuthJwt";
    private static final String KEY_USER = "dynamiqrAuthUser";

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
            prefs.edit().remove(KEY_USER).apply();
            return;
        }
        prefs.edit().putString(KEY_USER, gson.toJson(user)).apply();
    }

    public User getUser() {
        String json = prefs.getString(KEY_USER, null);
        if (json == null || json.isEmpty()) {
            return null;
        }
        return gson.fromJson(json, User.class);
    }

    public void saveSession(String token, User user) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER, gson.toJson(user))
                .apply();
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.isEmpty();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
