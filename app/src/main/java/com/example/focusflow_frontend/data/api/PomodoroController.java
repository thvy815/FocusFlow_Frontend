package com.example.focusflow_frontend.data.api;

import com.example.focusflow_frontend.data.model.Pomodoro;
import com.example.focusflow_frontend.data.model.SignInRequest;
import com.example.focusflow_frontend.data.model.SignInResponse;
import com.example.focusflow_frontend.data.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface PomodoroController {
    // Lấy tất cả các phiên pomodoro theo userId
    @GET("/api/pomodoro/user/{userId}")
    Call<List<Pomodoro>> getPomodorosByUser(@Path("userId") int userId);

    // Lấy 1 phiên pomodoro theo id
    @GET("/api/pomodoro/{id}")
    Call<Pomodoro> getPomodoroById(@Path("id") int id);

    // Tạo mới 1 phiên pomodoro
    @POST("/api/pomodoro")
    Call<Pomodoro> createPomodoro(@Body Pomodoro pomodoro);

    @PUT("/api/pomodoro/{id}")
    Call<Pomodoro> updatePomodoro(@Path("id") int id, @Body Pomodoro pomodoro);

    // Xoá 1 phiên pomodoro theo id
    @DELETE("/api/pomodoro/{id}")
    Call<Void> deletePomodoro(@Path("id") int id);

}