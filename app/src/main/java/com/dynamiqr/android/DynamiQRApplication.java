package com.dynamiqr.android;

import android.app.Application;
import android.content.Context;
import com.dynamiqr.android.data.api.ApiService;
import com.dynamiqr.android.data.api.RetrofitClient;
import com.dynamiqr.android.data.local.AppPreferences;
import com.dynamiqr.android.data.local.AuthManager;
import com.dynamiqr.android.data.repository.AuthRepository;
import com.dynamiqr.android.data.repository.LocalUserRepository;
import com.dynamiqr.android.data.repository.QrRepository;

public class DynamiQRApplication extends Application {

    private static DynamiQRApplication instance;

    private AuthManager authManager;
    private ApiService apiService;
    private AuthRepository authRepository;
    private LocalUserRepository localUserRepository;
    private QrRepository qrRepository;
    private AppPreferences appPreferences;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        appPreferences = new AppPreferences(this);
        appPreferences.applyStoredTheme();
        appPreferences.applyStoredLanguage();
        authManager = new AuthManager(this);
        // Drop fake local-only sessions from the previous course experiment; keep real JWTs.
        if (authManager.isLocalSession()) {
            authManager.clear();
        }
        localUserRepository = new LocalUserRepository(this);
        apiService = RetrofitClient.getService(authManager);
        authRepository = new AuthRepository(apiService);
        qrRepository = new QrRepository(apiService);
    }

    public static DynamiQRApplication getInstance() {
        return instance;
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    public ApiService getApiService() {
        return apiService;
    }

    public AuthRepository getAuthRepository() {
        return authRepository;
    }

    public LocalUserRepository getLocalUserRepository() {
        return localUserRepository;
    }

    public QrRepository getQrRepository() {
        return qrRepository;
    }

    public AppPreferences getAppPreferences() {
        return appPreferences;
    }
}
