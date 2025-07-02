package com.example.focusflow_frontend.data.viewmodel;

import android.content.Context;

import androidx.lifecycle.ViewModel;

import com.example.focusflow_frontend.R;

public class MissionViewModel extends ViewModel {
    private Context context;
    private int currentPetIndex = 0;
    private int userScore = 0;
    private int[] petLevelScores = {10, 20, 100, 200, 400};
    private boolean[] taskCompleted = {false, false, false}; // 3 mission
    private AuthViewModel authViewModel;
    public void setAuthViewModel(AuthViewModel authViewModel) {
        this.authViewModel = authViewModel;
    }

    private int[] petImages = {
            R.drawable.ic_pet_level1,
            R.drawable.ic_pet_level2,
            R.drawable.ic_pet_level3,
            R.drawable.ic_pet_level4,
            R.drawable.ic_pet_level5
    };

    private void determinePetIndex() {
        for (int i = 0; i < petLevelScores.length; i++) {
            if (userScore < petLevelScores[i]) {
                currentPetIndex = i;
                return;
            }
        }
        currentPetIndex = petLevelScores.length - 1;
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

    public void nextPet() {
        currentPetIndex = (currentPetIndex + 1) % petImages.length;
    }

    public void prevPet() {
        currentPetIndex = (currentPetIndex - 1 + petImages.length) % petImages.length;
    }

    private static final String PREF_NAME = "mission_pref";

    public void saveMissionState() {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean("mission_0", taskCompleted[0])
                .putBoolean("mission_1", taskCompleted[1])
                .putBoolean("mission_2", taskCompleted[2])
                .putInt("user_score", userScore)
                .apply();
    }

    public void loadMissionState() {
        var prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        taskCompleted[0] = prefs.getBoolean("mission_0", false);
        taskCompleted[1] = prefs.getBoolean("mission_1", false);
        taskCompleted[2] = prefs.getBoolean("mission_2", false);
        userScore = prefs.getInt("user_score", 0);

        determinePetIndex();
    }

    public void init(Context context) {
        this.context = context;
        loadMissionState();
    }

    public void applyTaskStatus(int index, boolean completed, int score) {
        if (taskCompleted[index]) return;

        taskCompleted[index] = completed;
        if (completed) {
            userScore += score;
            determinePetIndex();
            saveMissionState();

            if (authViewModel != null) {
                authViewModel.updateUserScore(userScore); // Gửi lên backend
            }
        }
    }


}
