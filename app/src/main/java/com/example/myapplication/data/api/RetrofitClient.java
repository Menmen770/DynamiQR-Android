package com.example.myapplication.data.api;

import com.example.myapplication.BuildConfig;
import com.example.myapplication.data.local.AuthManager;
import java.util.concurrent.TimeUnit;
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
                    // HEADERS only — BODY would dump multi‑MB PDF/base64 payloads.
                    logging.setLevel(BuildConfig.DEBUG
                            ? HttpLoggingInterceptor.Level.HEADERS
                            : HttpLoggingInterceptor.Level.NONE);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .addInterceptor(chain -> {
                                Request.Builder builder = chain.request().newBuilder();
                                String token = authManager.getToken();
                                // Course-local session is not a real JWT — do not send it to the API.
                                if (token != null && !token.isEmpty()
                                        && !AuthManager.LOCAL_SESSION_TOKEN.equals(token)) {
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
