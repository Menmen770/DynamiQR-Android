package com.example.myapplication.data.api;

import com.example.myapplication.data.models.GenerateQrResponse;
import com.example.myapplication.data.models.LoginResponse;
import com.example.myapplication.data.models.MeResponse;
import com.example.myapplication.data.models.QrListResponse;
import com.example.myapplication.data.models.SaveQrResponse;
import com.example.myapplication.data.models.User;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body Map<String, String> credentials);

    @POST("/api/auth/register")
    Call<LoginResponse> register(@Body Map<String, String> body);

    @POST("/api/auth/verify-email")
    Call<LoginResponse> verifyEmail(@Body Map<String, String> body);

    @POST("/api/auth/resend-verification")
    Call<Map<String, String>> resendVerification(@Body Map<String, String> body);

    @GET("/api/auth/me")
    Call<MeResponse> getMe();

    @POST("/api/auth/logout")
    Call<Map<String, Object>> logout();

    @PUT("/api/auth/profile")
    Call<Map<String, User>> updateProfile(@Body Map<String, String> body);

    @GET("/api/saved-qrs")
    Call<QrListResponse> getSavedQrs(@Query("q") String query, @Query("limit") int limit);

    @POST("/api/saved-qrs")
    Call<SaveQrResponse> saveQr(@Body Map<String, Object> body);

    @PATCH("/api/saved-qrs/{id}")
    Call<SaveQrResponse> patchQr(@Path("id") String id, @Body Map<String, Object> body);

    @DELETE("/api/saved-qrs/{id}")
    Call<Map<String, Object>> deleteQr(@Path("id") String id);

    @POST("/api/generate-qr")
    Call<GenerateQrResponse> generateQr(@Body Map<String, Object> body);
}
