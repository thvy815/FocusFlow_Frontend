package com.example.focusflow_frontend.data.api;

import com.example.focusflow_frontend.data.model.PomodoroDetail;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface PomodoroDetailController {

    // Lấy tất cả PomodoroDetail theo pomodoroId
    @GET("/api/pomodoroDetails/pomodoro/{pomodoroId}")
    Call<List<PomodoroDetail>> getPomodoroDetailsByPomodoroId(@Path("pomodoroId") int pomodoroId);

    // Lấy một PomodoroDetail theo id
    @GET("/api/pomodoroDetails/{id}")
    Call<PomodoroDetail> getPomodoroDetailById(@Path("id") int id);

    // Tạo mới một PomodoroDetail
    @POST("/api/pomodoroDetails")
    Call<PomodoroDetail> createPomodoroDetail(@Body PomodoroDetail pomodoroDetail);
}
