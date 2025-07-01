package com.example.focusflow_frontend.data.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.focusflow_frontend.data.api.StreakController;
import com.example.focusflow_frontend.data.api.UserController;
import com.example.focusflow_frontend.data.model.Streak;
import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.utils.ApiClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StreakViewModel extends AndroidViewModel {

    private final MutableLiveData<Integer> streakCountLive = new MutableLiveData<>();
    private final MutableLiveData<List<LocalDate>> datesLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Streak> streakLive = new MutableLiveData<>();
    private final StreakController streakController;

    public StreakViewModel(@NonNull Application application) {
        super(application);
        Context context = getApplication().getApplicationContext();
        streakController = ApiClient.getRetrofit(context).create(StreakController.class);
    }

    public interface StreakCallback {
        void onSuccess(Streak streak);
        void onFailure(String errorMessage);
    }

    public void getStreakByUser(int userId, StreakCallback callback) {
        streakController.getStreakByUserId(userId).enqueue(new Callback<Streak>() {
            @Override
            public void onResponse(Call<Streak> call, Response<Streak> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Không tìm thấy streak.");
                }
            }

            @Override
            public void onFailure(Call<Streak> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

}
