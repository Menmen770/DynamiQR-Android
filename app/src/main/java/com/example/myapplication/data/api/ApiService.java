package com.example.myapplication.data.api;

import com.example.myapplication.data.models.LoginResponse;
import com.example.myapplication.data.models.QrListResponse;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body Map<String, String> credentials);

    @POST("/api/auth/register")
    Call<LoginResponse> register(@Body Map<String, String> body);

    @POST("/api/auth/verify-email")
    Call<LoginResponse> verifyEmail(@Body Map<String, String> body);

    @GET("/api/saved-qrs")
    Call<QrListResponse> getSavedQrs(@Query("q") String query);
}
