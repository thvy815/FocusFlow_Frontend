package com.example.focusflow_frontend.presentation.mission;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.ViewModel;

import com.example.focusflow_frontend.R;

import java.time.LocalDate;

public class MissionViewModel extends ViewModel {
    private static final String PREFS_NAME = "pet_prefs";
    private SharedPreferences prefs;

    private int currentPetIndex;
    private int currentGrowth;
    private int currentLevel;
    private boolean[] taskCompleted;
    private String[] petNames;

    private final int[] petImages = {
            R.drawable.ic_pet_level1,
            R.drawable.ic_pet_level2,
            R.drawable.ic_pet_level3,
            R.drawable.ic_pet_level4,
            R.drawable.ic_pet_level5
    };
    private final int[] levelThresholds = {10, 20, 100, 200, 500};

    /**
     * Must be called before using other methods, e.g. in Fragment.onCreateView
     */
    public void init(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        taskCompleted = new boolean[3];
        petNames = new String[petImages.length];

        resetTasksIfNeeded();
        loadState();
    }

    private void resetTasksIfNeeded() {
        String lastDate = prefs.getString("last_reset_date", "");
        String today = LocalDate.now().toString();
        if (!today.equals(lastDate)) {
            SharedPreferences.Editor editor = prefs.edit();
            for (int i = 0; i < taskCompleted.length; i++) {
                editor.putBoolean("task_" + i, false);
            }
            editor.putString("last_reset_date", today);
            editor.apply();
        }
    }

    private void loadState() {
        for (int i = 0; i < taskCompleted.length; i++) {
            taskCompleted[i] = prefs.getBoolean("task_" + i, false);
        }
        currentGrowth = prefs.getInt("current_growth", 0);
        currentLevel = prefs.getInt("current_level", 0);
        currentPetIndex = prefs.getInt("current_pet_index", 0);
        for (int i = 0; i < petNames.length; i++) {
            petNames[i] = prefs.getString("pet_name_" + i, "Pet " + (i + 1));
        }
    }

    public void nextPet() {
        currentPetIndex = (currentPetIndex + 1) % petImages.length;
        prefs.edit().putInt("current_pet_index", currentPetIndex).apply();
    }

    public void prevPet() {
        currentPetIndex = (currentPetIndex - 1 + petImages.length) % petImages.length;
        prefs.edit().putInt("current_pet_index", currentPetIndex).apply();
    }

    public void renamePet(String newName) {
        petNames[currentPetIndex] = newName;
        prefs.edit().putString("pet_name_" + currentPetIndex, newName).apply();
    }

    /**
     * Handle task completion status from backend or other module
     */
    public void applyTaskStatus(int taskIndex, boolean isCompleted, int point) {
        if (isCompleted && !taskCompleted[taskIndex]) {
            taskCompleted[taskIndex] = true;
            currentGrowth += point;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("task_" + taskIndex, true)
                    .putInt("current_growth", currentGrowth)
                    .apply();
            updateLevel();
        }
    }

    private void updateLevel() {
        while (currentLevel < levelThresholds.length
                && currentGrowth >= levelThresholds[currentLevel]) {
            currentLevel++;
        }
        prefs.edit().putInt("current_level", currentLevel).apply();
    }

    // --- Getters for UI binding ---
    public int getPetImageRes() {
        return petImages[currentPetIndex];
    }

    public String getPetName() {
        return petNames[currentPetIndex];
    }

    public int getCurrentGrowth() {
        return currentGrowth;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getProgress() {
        int prevMax = currentLevel == 0 ? 0 : levelThresholds[currentLevel - 1];
        return currentGrowth - prevMax;
    }

    public int getProgressMax() {
        int max = currentLevel < levelThresholds.length
                ? levelThresholds[currentLevel]
                : levelThresholds[levelThresholds.length - 1];
        int prevMax = currentLevel == 0 ? 0 : levelThresholds[currentLevel - 1];
        return max - prevMax;
    }

    public boolean isTaskCompleted(int taskIndex) {
        return taskCompleted[taskIndex];
    }
}