package com.example.focusflow_frontend.data.api;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MissionApi {
    @GET("api/mission/evaluate/{userId}")
    Call<Integer> getUserScore(@Path("userId") int userId);
}
