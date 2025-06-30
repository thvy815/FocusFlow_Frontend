package com.example.focusflow_frontend.data.api;

import com.example.focusflow_frontend.data.model.Streak;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface StreakController {
    @GET("/api/streak/get")
    Call<Streak> getStreak(@Query("userId") int userId);

    @GET("/api/streak/dates")
    Call<List<String>> getStreakDates(@Query("userId") int userId);

    @POST("/api/streak/update")
    Call<Streak> updateStreak(@Query("userId") int userId);
}
