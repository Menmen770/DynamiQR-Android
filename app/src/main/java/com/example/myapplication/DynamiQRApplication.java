package com.example.myapplication;

import android.app.Application;
import android.content.Context;
import com.example.myapplication.core.i18n.AppI18n;
import com.example.myapplication.data.api.ApiService;
import com.example.myapplication.data.api.RetrofitClient;
import com.example.myapplication.data.local.AppPreferences;
import com.example.myapplication.data.local.AuthManager;
import com.example.myapplication.data.repository.AuthRepository;
import com.example.myapplication.data.repository.QrRepository;

public class DynamiQRApplication extends Application {

    private static DynamiQRApplication instance;

    private AuthManager authManager;
    private ApiService apiService;
    private AuthRepository authRepository;
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
        AppI18n.init(this);
        authManager = new AuthManager(this);
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

    public QrRepository getQrRepository() {
        return qrRepository;
    }

    public AppPreferences getAppPreferences() {
        return appPreferences;
    }
}
