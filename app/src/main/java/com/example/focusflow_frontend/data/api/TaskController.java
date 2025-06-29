package com.example.focusflow_frontend.data.api;

import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.data.model.TaskGroupRequest;
import com.example.focusflow_frontend.data.model.User;

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

    @GET("api/tasks/group/{groupId}")
    Call<List<Task>> getTasksByGroup(@Path("groupId") Integer groupId);

    // API thêm task mới
    @POST("api/tasks")
    Call<Task> createTask(@Body TaskGroupRequest request);

    // API update task
    @PUT("api/tasks")
    Call<Void> updateTask(@Body TaskGroupRequest request);

    // API delete task
    @DELETE("api/tasks/{id}")
    Call<Void> deleteTask(@Path("id") Integer id);
}
