package com.example.myapplication;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import com.example.myapplication.data.api.ApiService;
import com.example.myapplication.data.api.RetrofitClient;
import com.example.myapplication.data.local.AuthManager;
import com.example.myapplication.data.repository.AuthRepository;
import com.example.myapplication.data.repository.QrRepository;
import java.util.Locale;

public class DynamiQRApplication extends Application {

    private static DynamiQRApplication instance;

    private AuthManager authManager;
    private ApiService apiService;
    private AuthRepository authRepository;
    private QrRepository qrRepository;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(wrapHebrew(base));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("he"));
        authManager = new AuthManager(this);
        apiService = RetrofitClient.getService(authManager);
        authRepository = new AuthRepository(apiService);
        qrRepository = new QrRepository(apiService);
    }

    private static Context wrapHebrew(Context base) {
        Locale locale = new Locale("he");
        Locale.setDefault(locale);
        Configuration config = new Configuration(base.getResources().getConfiguration());
        config.setLocale(locale);
        config.setLayoutDirection(locale);
        return base.createConfigurationContext(config);
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
