package com.example.focusflow_frontend.presentation.pomo;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.focusflow_frontend.data.api.PomodoroController;
import com.example.focusflow_frontend.data.api.PomodoroDetailController;
import com.example.focusflow_frontend.data.model.Pomodoro;
import com.example.focusflow_frontend.data.model.PomodoroDetail;
import com.example.focusflow_frontend.utils.ApiClient;

import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PomodoroViewModel extends ViewModel {
    // Hàm lưu pomodoro - userId, taskId, startAt, endAt, dueDate, totalTime, isDeleted
    private final MutableLiveData<Pomodoro> lastCreatedPomodoro = new MutableLiveData<>();

    public LiveData<Pomodoro> getLastCreatedPomodoro() {
        return lastCreatedPomodoro;
    }
    public void createPomodoro(Context context, int userId, int taskId,
                             long startAt, LocalDate dueDate, boolean isDeleted) {

        Time st = new Time(startAt);
        String startAtStr = st.toString();
        String dueDateStr = dueDate.toString();

        Pomodoro pomodoro = new Pomodoro(userId, taskId, startAtStr, dueDateStr, isDeleted);

        PomodoroController controller = ApiClient.getPomodoroController(context);

        controller.createPomodoro(pomodoro).enqueue(new Callback<Pomodoro>() {
            @Override
            public void onResponse(Call<Pomodoro> call, Response<Pomodoro> response) {
                if (response.isSuccessful()) {
                    Pomodoro createdPomodoro = response.body();
                    lastCreatedPomodoro.postValue(createdPomodoro);
                    Log.d("PomodoroVM", "Pomodoro saved successfully.");
                } else {
                    Log.e("PomodoroVM", "Failed to save: " + response.code() + ", " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<Pomodoro> call, Throwable t) {
                Log.e("PomodoroVM", "Error: " + t.getMessage());
            }
        });
    }

    // Hàm update pomodoro
    public void updatePomodoro(Context context, Pomodoro pomodoro) {

        PomodoroController controller = ApiClient.getPomodoroController(context);

        controller.updatePomodoro(pomodoro.getId(), pomodoro).enqueue(new Callback<Pomodoro>() {
            @Override
            public void onResponse(Call<Pomodoro> call, Response<Pomodoro> response) {
                if (response.isSuccessful()) {
                    Log.d("PomodoroVM", "Pomodoro saved successfully.");
                } else {
                    Log.e("PomodoroVM", "Failed to save: " + response.code() + ", " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<Pomodoro> call, Throwable t) {
                Log.e("PomodoroVM", "Error: " + t.getMessage());
            }
        });
    }
    // Hàm lưu details
    public void createPomodoroDetail(Context context, int userId, int taskId, int pomodoroId, long startAt, long endAt, long totalTime){
        Time st = new Time(startAt);
        String startAtStr = st.toString();

        Time en = new Time(endAt);
        String endAtStr = en.toString();

        PomodoroDetail pomodoroDetail = new PomodoroDetail(userId, taskId, pomodoroId,startAtStr,endAtStr,totalTime);

        PomodoroDetailController controller = ApiClient.getPomodoroDetailController(context);

        controller.createPomodoroDetail(pomodoroDetail).enqueue(new Callback<PomodoroDetail>() {
            @Override
            public void onResponse(Call<PomodoroDetail> call, Response<PomodoroDetail> response) {
                if (response.isSuccessful()) {
                    PomodoroDetail createdPomodoroDetail = response.body();
                    Log.d("PomodoroDetailVM", "Pomodoro Details saved successfully.");
                } else {
                    Log.e("PomodoroDetailVM", "Failed to save detail: " + response.code() + ", " + response.errorBody().toString());
                }
            }
            @Override
            public void onFailure(Call<PomodoroDetail> call, Throwable t) {
                Log.e("PomodoroVM", "Error: " + t.getMessage());
            }
        });

    }

    //Hàm get:

    private final MutableLiveData<List<Pomodoro>> pomodoroList = new MutableLiveData<>();

    public LiveData<List<Pomodoro>> getPomodoroList() {
        return pomodoroList;
    }

    public void getAllPomodoro(Context context, int userId) {

        PomodoroController controller = ApiClient.getPomodoroController(context);

        controller.getPomodorosByUser(userId).enqueue(new Callback<List<Pomodoro>>() {
            @Override
            public void onResponse(Call<List<Pomodoro>> call, Response<List<Pomodoro>> response) {
                if (response.isSuccessful()) {
                    pomodoroList.setValue(response.body());
                    Log.d("PomodoroVM", "Get all records.");
                } else {
                    Log.e("PomodoroVM", "Failed to get: " + response.code() + ", " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<List<Pomodoro>> call, Throwable t) {
                Log.e("PomodoroVM", "Error: " + t.getMessage());
            }
        });
    }

    //GET DETAILS:
    private final MutableLiveData<List<PomodoroDetail>> pomodoroDetailList = new MutableLiveData<>();

    public LiveData<List<PomodoroDetail>> getPomodoroDetailList() {
        return pomodoroDetailList;
    }
    public void getPomodoroDetailsByPomodoroId(Context context, int pomodoroId) {

        PomodoroDetailController controller = ApiClient.getPomodoroDetailController(context);

        controller.getPomodoroDetailsByPomodoroId(pomodoroId).enqueue(new Callback<List<PomodoroDetail>>() {
            @Override
            public void onResponse(Call<List<PomodoroDetail>> call, Response<List<PomodoroDetail>> response) {
                if (response.isSuccessful()) {
                    pomodoroDetailList.setValue(response.body());
                    Log.d("PomodoroVM", "Get all details.");
                } else {
                    Log.e("PomodoroVM", "Failed to get: " + response.code() + ", " + response.errorBody().toString());
                }
            }
            @Override
            public void onFailure(Call<List<PomodoroDetail>> call, Throwable t) {
                Log.e("PomodoroVM", "Error: " + t.getMessage());
            }
        });
    }



}
