package com.example.focusflow_frontend.data.api;

import com.example.focusflow_frontend.data.model.Task;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.Call;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface TaskController {

    // API lấy tất cả các task của user
    @GET("api/tasks/user/{userId}")
    Call<List<Task>> getTasksByUser(@Path("userId") Integer userId);

    // API thêm task mới
    @POST("api/tasks")
    Call<Task> createTask(@Body Task task);

    // API update task
    @PUT("api/tasks/{id}")
    Call<Void> updateTask(@Path("id") Integer id, @Body Task task);

    // API delete task
    @DELETE("api/tasks/{id}")
    Call<Void> deleteTask(@Path("id") Integer id);
}
