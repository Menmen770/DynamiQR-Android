package com.example.myapplication.data.api;

import com.example.myapplication.BuildConfig;
import com.example.myapplication.data.local.AuthManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {

    private static volatile Retrofit retrofit;

    private RetrofitClient() {
    }

    public static ApiService getService(AuthManager authManager) {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(BuildConfig.DEBUG
                            ? HttpLoggingInterceptor.Level.BODY
                            : HttpLoggingInterceptor.Level.NONE);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(chain -> {
                                Request.Builder builder = chain.request().newBuilder();
                                String token = authManager.getToken();
                                if (token != null && !token.isEmpty()) {
                                    builder.addHeader("Authorization", "Bearer " + token);
                                }
                                return chain.proceed(builder.build());
                            })
                            .addInterceptor(logging)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BuildConfig.API_BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit.create(ApiService.class);
    }
}
