package com.example.myapplication;

import android.app.Application;
import com.example.myapplication.data.api.ApiService;
import com.example.myapplication.data.api.RetrofitClient;
import com.example.myapplication.data.local.AuthManager;
import com.example.myapplication.data.repository.AuthRepository;
import com.example.myapplication.data.repository.QrRepository;

public class DynamiQRApplication extends Application {

    private static DynamiQRApplication instance;

    private AuthManager authManager;
    private ApiService apiService;
    private AuthRepository authRepository;
    private QrRepository qrRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
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
}
