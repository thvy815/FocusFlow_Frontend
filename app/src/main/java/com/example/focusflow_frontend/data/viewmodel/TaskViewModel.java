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

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskViewModel extends AndroidViewModel {
    private MutableLiveData<List<Task>> taskList = new MutableLiveData<>();
    private MutableLiveData<List<Task>> groupTaskList = new MutableLiveData<>();
    public MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> taskCreatedSuccess = new MutableLiveData<>();
    private MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();
    private MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>();
    private TaskController taskController;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        // Khởi tạo TaskController thông qua ApiClient
        Context context = getApplication().getApplicationContext();
        taskController = ApiClient.getRetrofit(context).create(TaskController.class);
    }

    public LiveData<List<Task>> getTaskList() { return taskList; }
    public LiveData<List<Task>> getGroupTaskList() { return groupTaskList; }
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    public LiveData<Boolean> getTaskCreatedSuccess() {
        return taskCreatedSuccess;
    }
    public LiveData<Boolean> getUpdateSuccess() { return updateSuccess; }
    public LiveData<Boolean> getDeleteSuccess() { return deleteSuccess; }

    public void fetchTasks(int userId) {
        taskController.getTasksByUser(userId).enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    taskList.setValue(response.body());
                }
                else {
                    errorMessage.postValue("Không lấy được danh sách task.");
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                errorMessage.postValue("Error: " + t.getMessage());
            }
        });
    }

    public void fetchTasksByGroup(int groupId) {
        taskController.getTasksByGroup(groupId).enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    groupTaskList.setValue(response.body());
                } else {
                    errorMessage.postValue("Không lấy được danh sách task nhóm.");
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                errorMessage.postValue("Lỗi khi gọi API nhóm: " + t.getMessage());
            }
        });
    }

    private MutableLiveData<Task> createdTask = new MutableLiveData<>();
    public LiveData<Task> getCreatedTask() { return createdTask; }
    public void createTask(Task task) {
        taskController.createTask(task).enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Task created = response.body(); // task từ backend, đã có ID
                    createdTask.postValue(created); // truyền task về cho View
                    fetchTasks(task.getUserId()); // load lại danh sách sau khi thêm
                    taskCreatedSuccess.postValue(true);  // báo thành công
                } else {
                    createdTask.postValue(null);
                    taskCreatedSuccess.postValue(false); // báo thất bại
                    errorMessage.postValue("Failed to add task");
                }
            }

            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                createdTask.postValue(null);
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
                    updateSuccess.postValue(true);  // báo thành công
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("TaskUpdate", "Update failed: " + t.getMessage());
                updateSuccess.postValue(false); // báo thất bại
            }
        });
    }

    public void deleteTask(int taskId) {
        taskController.deleteTask(taskId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("TaskDelete", "Deleted successfully");
                    deleteSuccess.postValue(true);
                } else {
                    Log.e("TaskDelete", "Delete failed: response not successful");
                    deleteSuccess.postValue(false);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("TaskDelete", "Delete failed: " + t.getMessage());
                deleteSuccess.postValue(false);
            }
        });
    }
}
