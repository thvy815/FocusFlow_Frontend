package com.example.focusflow_frontend.utils;

import android.content.Context;

import com.example.focusflow_frontend.data.api.StreakController;
import com.example.focusflow_frontend.data.api.UserController;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit = null;

    public static Retrofit getRetrofit(Context context) {
        if (retrofit == null) {
            // Lấy token từ SharedPreferences
            String token = TokenManager.getToken(context);

            // Tạo Interceptor để thêm header Authorization với token
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            if (token != null) {
                httpClient.addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws java.io.IOException {
                        Request original = chain.request();

                        // Tạo request mới với thêm header Authorization
                        Request.Builder requestBuilder = original.newBuilder()
                                .header("Authorization", "Bearer " + token);

                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    }
                });
            }

            // Tạo Retrofit với OkHttpClient và GsonConverterFactory
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // Lấy UserController từ Retrofit
    public static UserController getUserController(Context context) {
        return getRetrofit(context).create(UserController.class);
    }

    public static StreakController getStreakApi() {
        return getRetrofit().create(StreakController.class);
    }
}
