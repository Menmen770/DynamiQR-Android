package com.dynamiqr.android.data.repository;

import com.dynamiqr.android.data.api.ApiService;
import com.dynamiqr.android.data.models.GenerateQrResponse;
import com.dynamiqr.android.data.models.LoginResponse;
import com.dynamiqr.android.data.models.MeResponse;
import com.dynamiqr.android.data.models.QrListResponse;
import com.dynamiqr.android.data.models.SaveQrResponse;
import com.dynamiqr.android.data.models.User;
import java.util.Map;
import retrofit2.Call;

public class AuthRepository {

    private final ApiService apiService;

    public AuthRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public Call<LoginResponse> login(Map<String, String> credentials) {
        return apiService.login(credentials);
    }

    public Call<LoginResponse> register(Map<String, String> body) {
        return apiService.register(body);
    }

    public Call<LoginResponse> verifyEmail(Map<String, String> body) {
        return apiService.verifyEmail(body);
    }

    public Call<Map<String, String>> resendVerification(Map<String, String> body) {
        return apiService.resendVerification(body);
    }

    public Call<MeResponse> getMe() {
        return apiService.getMe();
    }

    public Call<Map<String, Object>> logout() {
        return apiService.logout();
    }

    public Call<Map<String, User>> updateProfile(Map<String, String> body) {
        return apiService.updateProfile(body);
    }
}
