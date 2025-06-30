package com.example.focusflow_frontend.data.api;


import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.focusflow_frontend.data.api.StreakController;
import com.example.focusflow_frontend.data.model.Streak;
import com.example.focusflow_frontend.utils.ApiClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StreakRepository {

    private final StreakController streakController;

    public StreakRepository(Context context) {
        streakController = ApiClient.getStreakController(context);
    }

    public void fetchStreak(int userId, MutableLiveData<Streak> streakLive) {
        streakController.getStreak(userId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Streak> call, Response<Streak> response) {
                if (response.isSuccessful()) {
                    streakLive.setValue(response.body());
                } else {
                    Log.e("StreakRepo", "Failed to fetch streak: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Streak> call, Throwable t) {
                Log.e("StreakRepo", "Error: ", t);
            }
        });
    }

    public void fetchStreakDates(int userId, MutableLiveData<List<LocalDate>> datesLive) {
        streakController.getStreakDates(userId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LocalDate> parsed = new ArrayList<>();
                    for (String s : response.body()) {
                        parsed.add(LocalDate.parse(s));
                    }
                    datesLive.setValue(parsed);
                } else {
                    Log.e("StreakRepo", "Failed to fetch dates: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Log.e("StreakRepo", "Error: ", t);
            }
        });
    }

    public void updateStreak(int userId) {
        streakController.updateStreak(userId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Streak> call, Response<Streak> response) {
                if (!response.isSuccessful()) {
                    Log.e("StreakRepo", "Failed to update streak: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Streak> call, Throwable t) {
                Log.e("StreakRepo", "Error: ", t);
            }
        });
    }
}
