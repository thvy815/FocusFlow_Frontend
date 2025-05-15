package com.example.focusflow_frontend.data.viewmodel;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.focusflow_frontend.data.api.TaskController;
import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.utils.ApiClient;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskViewModel extends AndroidViewModel {
    private MutableLiveData<List<Task>> taskList = new MutableLiveData<>();
    public MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> taskCreatedSuccess = new MutableLiveData<>();
    private TaskController taskController;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        // Khởi tạo TaskController thông qua ApiClient
        Context context = getApplication().getApplicationContext();
        taskController = ApiClient.getRetrofit(context).create(TaskController.class);
    }

    // Trả về LiveData của danh sách task
    public LiveData<List<Task>> getTaskList() {
        return taskList;
    }

    // Trả về LiveData của lỗi (nếu có)
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getTaskCreatedSuccess() {
        return taskCreatedSuccess;
    }

    public void fetchTasks(int userId) {
        taskController.getTasksByUser(userId).enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    taskList.setValue(response.body());
                }
                else {
                    String error = "Code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            error += ", Body: " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        error += ", Exception: " + e.getMessage();
                    }
                    Log.e("FetchTasks", error);
                    errorMessage.postValue("Không lấy được danh sách task.");
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                errorMessage.postValue("Error: " + t.getMessage());
            }
        });
    }

    public void createTask(Task task) {
        taskController.createTask(task).enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (response.isSuccessful()) {
                    fetchTasks(task.getUserId()); // load lại danh sách sau khi thêm
                    taskCreatedSuccess.postValue(true);  // báo thành công
                } else {
                    taskCreatedSuccess.postValue(false); // báo thất bại
                    errorMessage.postValue("Failed to add task");
                }
            }

            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                taskCreatedSuccess.postValue(false); // báo thất bại
                errorMessage.postValue("Error: " + t.getMessage());
            }
        });
    }

    public void updateTask(Task task) {
        taskController.updateTask(task.getId(), task).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("TaskUpdate", "Updated successfully");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("TaskUpdate", "Update failed: " + t.getMessage());
            }
        });
    }
}
