package com.example.focusflow_frontend.utils;

import android.content.Context;

import com.example.focusflow_frontend.data.api.PomodoroController;
import com.example.focusflow_frontend.data.api.PomodoroDetailController;

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
            // Tạo Interceptor để thêm header Authorization với token
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            httpClient.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws java.io.IOException {
                    Request original = chain.request();

                    // Lấy token từ SharedPreferences
                    String token = TokenManager.getToken(context);

                    // Tạo request mới với thêm header Authorization
                    Request.Builder requestBuilder = original.newBuilder();
                    if (token != null && !token.isEmpty()) {
                        requestBuilder.header("Authorization", "Bearer " + token);
                    }

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });

            // Tạo Retrofit với OkHttpClient và GsonConverterFactory
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static PomodoroController getPomodoroController(Context context) {
        return getRetrofit(context).create(PomodoroController.class);
    }

    public static PomodoroDetailController getPomodoroDetailController(Context context) {
        return getRetrofit(context).create(PomodoroDetailController.class);
    }
}
