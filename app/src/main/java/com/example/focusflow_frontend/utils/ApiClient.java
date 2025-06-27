package com.example.focusflow_frontend.utils;

import android.content.Context;

import com.example.focusflow_frontend.data.api.PomodoroController;
import com.example.focusflow_frontend.data.api.PomodoroDetailController;
import com.example.focusflow_frontend.data.api.ProController;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit = null;

    public static Retrofit getRetrofit(Context context) {
        if (retrofit == null) {
            // Tạo Interceptor để thêm header Authorization với token
            Interceptor authInterceptor = chain -> {
                Request original = chain.request();
                String token = TokenManager.getToken(context);

                Request.Builder requestBuilder = original.newBuilder();
                if (token != null && !token.isEmpty()) {
                    requestBuilder.header("Authorization", "Bearer " + token);
                }

                Request request = requestBuilder.build();
                return chain.proceed(request);
            };

            // OkHttpClient với timeout (60s) và auth
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(authInterceptor)
                    .build();

            // Tạo Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    public static void resetRetrofit() {
        retrofit = null;
    }

    public static PomodoroController getPomodoroController(Context context) {
        return getRetrofit(context).create(PomodoroController.class);
    }

    public static PomodoroDetailController getPomodoroDetailController(Context context) {
        return getRetrofit(context).create(PomodoroDetailController.class);
    }
    public static ProController getProController(Context context) {
        return getRetrofit(context).create(ProController.class);
    }
}
