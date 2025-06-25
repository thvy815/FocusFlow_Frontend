package com.example.focusflow_frontend.data.api;

import org.json.JSONObject;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpProvider {
    public static JSONObject sendPost(String URL, RequestBody formBody) {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectionSpecs(Collections.singletonList(ConnectionSpec.MODERN_TLS))
                    .callTimeout(10, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url(URL)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .post(formBody)
                    .build();

            Response response = client.newCall(request).execute();
            String body = response.body().string();
            return new JSONObject(body);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
