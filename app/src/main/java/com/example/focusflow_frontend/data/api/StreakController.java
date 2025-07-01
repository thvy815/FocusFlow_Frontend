package com.example.focusflow_frontend.data.api;

import com.example.focusflow_frontend.data.model.Streak;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface StreakController {
    @GET("/api/streak/{userId}")
    Call<Streak> getStreakByUserId(@Path("userId") int userId);
}
