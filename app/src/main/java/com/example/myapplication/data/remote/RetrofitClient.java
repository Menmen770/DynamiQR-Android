package com.example.myapplication.data.remote;

import com.example.myapplication.data.local.AuthManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://192.168.1.34:5000/";
    private static Retrofit retrofit = null;

    public static ApiService getService(AuthManager authManager) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(chain -> {
                    Request.Builder ongoing = chain.request().newBuilder();
                    String token = authManager.getToken();
                    if (token != null) {
                        ongoing.addHeader("Authorization", "Bearer " + token);
                    }
                    return chain.proceed(ongoing.build());
                })
                .build();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
