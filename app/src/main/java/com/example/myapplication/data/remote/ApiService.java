package com.example.myapplication.data.remote;

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

    @GET("/api/saved-qrs")
    Call<QrListResponse> getSavedQrs(@Query("q") String query);
}
