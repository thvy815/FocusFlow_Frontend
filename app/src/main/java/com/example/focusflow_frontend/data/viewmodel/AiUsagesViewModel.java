package com.example.focusflow_frontend.data.viewmodel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.example.focusflow_frontend.data.api.AiController;
import com.example.focusflow_frontend.utils.ApiClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiUsagesViewModel extends ViewModel {
    public void incrementAiUsage(Context context, int count) {
        AiController controller = ApiClient.getAiController(context);

        Map<String, Integer> body = new HashMap<>();
        body.put("count", count);

        controller.incrementAiUsage(body).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d("AI_USAGE", "Success: " + response.body());
                } else {
                    Log.e("AI_USAGE", "Failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("AI_USAGE", "Error: " + t.getMessage());
            }
        });
    }
}
