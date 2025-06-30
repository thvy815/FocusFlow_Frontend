package com.example.focusflow_frontend.data.api;


import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface MissionController {
    @POST("/api/mission/evaluate/{userId}")
    Call<String> evaluateMission(@Path("userId") int userId);
}
