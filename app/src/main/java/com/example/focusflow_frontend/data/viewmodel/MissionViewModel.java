package com.example.focusflow_frontend.data.viewmodel;

import android.content.Context;

import androidx.lifecycle.ViewModel;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.api.MissionController;
import com.example.focusflow_frontend.utils.ApiClient;
import com.example.focusflow_frontend.data.model.User;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MissionViewModel extends ViewModel {
    private Context context;
    private int currentPetIndex = 0;
    private int userScore = 0;
    private int[] petLevelScores = {10, 20, 100, 200, 400};

    private int userId;
    private int[] petImages = {
            R.drawable.ic_pet_level1,
            R.drawable.ic_pet_level2,
            R.drawable.ic_pet_level3,
            R.drawable.ic_pet_level4,
            R.drawable.ic_pet_level5
    };

    private boolean[] taskCompleted = {false, false, false}; // task, pomo, allTasks

    public void init(Context context) {
        this.context = context;
    }

    public void fetchScoreFromBackend(Context context,int userId, Runnable callback) {
        MissionController controller = ApiClient.getMissionController(context);

        controller.evaluateMission(userId).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body();
                        String numberPart = responseBody.replaceAll("\\D+", "");
                        userScore = Integer.parseInt(numberPart);
                        determinePetIndex();
                        if (callback != null) callback.run(); // Gọi callback update UI
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }


    private void determinePetIndex() {
        for (int i = 0; i < petLevelScores.length; i++) {
            if (userScore < petLevelScores[i]) {
                currentPetIndex = i;
                return;
            }
        }
        currentPetIndex = petLevelScores.length - 1;
    }

    public int getCurrentPetIndex() {
        return currentPetIndex;
    }

    public int getPetImageRes() {
        return petImages[currentPetIndex];
    }





    public int getProgress() {
        return userScore;
    }

    public int getProgressMax() {
        return petLevelScores[currentPetIndex];
    }

    public boolean isTaskCompleted(int index) {
        return taskCompleted[index];
    }

    public void applyTaskStatus(int index, boolean completed, int score) {
        taskCompleted[index] = completed;
        if (completed) userScore += score;
    }

    public void resetTestData() {
        taskCompleted = new boolean[]{false, false, false};
    }

    public void nextPet() {
        currentPetIndex = (currentPetIndex + 1) % petImages.length;
    }

    public void prevPet() {
        currentPetIndex = (currentPetIndex - 1 + petImages.length) % petImages.length;
    }
}
