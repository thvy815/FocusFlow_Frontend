package com.example.focusflow_frontend.data.viewmodel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Pomodoro;
import com.example.focusflow_frontend.data.model.Task;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MissionViewModel extends ViewModel {
    private Context context;
    private int currentPetIndex = 0;
    private int userScore ;
    private int[] petLevelScores = {10, 20, 100, 200, 400};
    private boolean[] taskCompleted = {false, false, false};

    private AuthViewModel authViewModel;
    public void setAuthViewModel(AuthViewModel authViewModel) {
        this.authViewModel = authViewModel;
    }
    private PomodoroViewModel pomodoroViewModel;

    public void setPomodoroViewModel(PomodoroViewModel pomodoroViewModel) {
        this.pomodoroViewModel = pomodoroViewModel;
    }
    private TaskViewModel taskViewModel;

    public void setTaskViewModel(TaskViewModel taskViewModel) {
        this.taskViewModel = taskViewModel;
    }

    public void setUserScore(int scoreFromServer) {
        this.userScore = scoreFromServer;
        determinePetIndex();
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


    public void nextPet() {
        if (currentPetIndex < petImages.length - 1) {
            currentPetIndex++;
        }
    }
    public void prevPet() {
        if (currentPetIndex > 0) {
            currentPetIndex--;
        }
    }
    public int getCurrentPetImage() {
        return petImages[currentPetIndex];
    }
    public int countCompletedTasksToday() {
        if (taskViewModel == null || taskViewModel.getTaskList().getValue() == null) {
            Log.d("MissionViewModel", "TaskViewModel or taskList is null");
            return 0;
        }

        List<Task> tasks = taskViewModel.getTaskList().getValue();
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        int count = 0;

        for (Task task : tasks) {
            Log.d("MissionViewModel", "Task due: " + task.getDueDate() + " | Completed: " + task.isCompleted());
            if (task.getDueDate() != null
                    && task.getDueDate().equals(today)
                    && Boolean.TRUE.equals(task.isCompleted())) {
                count++;
            }
        }

        Log.d("MissionViewModel", "Completed tasks today: " + count);
        return count;
    }



    private int countTasksForTomorrow() {
        int count = 0;
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<Task> tasks = taskViewModel.getTaskList().getValue();
        if (tasks == null) return 0;

        for (Task task : tasks) {
            String dueDateStr = task.getDueDate();
            if (dueDateStr != null) {
                try {
                    LocalDate dueDate = LocalDate.parse(dueDateStr, formatter);
                    if (dueDate.equals(tomorrow)) {
                        count++;
                    }
                } catch (DateTimeParseException e) {
                    Log.e("Mission", "❌ Lỗi định dạng ngày: " + dueDateStr, e);
                }
            }
        }

        Log.d("Mission", "✅ Số task ngày mai: " + count);
        return count;
    }





    public int getTotalPomodoroTimeToday() {
        if (pomodoroViewModel == null || pomodoroViewModel.getPomodoroList().getValue() == null) {
            Log.d("MissionViewModel", "PomodoroViewModel or list is null");
            return 0;
        }

        List<Pomodoro> pomodoros = pomodoroViewModel.getPomodoroList().getValue();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int total = 0;

        for (Pomodoro p : pomodoros) {
            Log.d("MissionViewModel", "Pomodoro startAt: " + p.getStartAt() + " | time: " + p.getTotalTime());
            if (p.getStartAt() != null && p.getStartAt().contains("T")) {
                String datePart = p.getStartAt().split("T")[0];
                if (today.equals(datePart)) {
                    total += (int) p.getTotalTime();
                }
            }
        }

        Log.d("MissionViewModel", "Total pomo time (s): " + total);
        return total;
    }

    public boolean isMissionCompleted(int index) {
        Log.d("Mission", "Completed tasks today = " + countCompletedTasksToday());

        switch (index) {
            case 0:
                return countCompletedTasksToday() >= 3;

            case 1:
                return getTotalPomodoroTimeToday() >= 3000; //100 phut

            case 2:
                return countTasksForTomorrow() >= 5;

            default:
                return false;
        }
    }
    public void applyTaskStatus(int index, boolean completed, int score) {
        if (taskCompleted[index]) return; // Đã hoàn thành rồi thì bỏ qua

        if (completed) {
            taskCompleted[index] = true;
            userScore += score;
            Log.d("AuthViewModel", "Calling API to update score = " + score);

            determinePetIndex();
            saveMissionState(); // nếu bạn muốn lưu lại trạng thái (optional)

            if (authViewModel != null) {
                authViewModel.updateUserScore(userScore); // Gửi điểm mới lên server
            }
        }
    }

    private static final String PREF_NAME = "mission_pref";
    private static final String PREF_DATE = "mission_last_date";

    public void saveMissionState() {
        if (context == null) return; // Tránh lỗi nếu context chưa được set

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean("mission_0", taskCompleted[0])
                .putBoolean("mission_1", taskCompleted[1])
                .putBoolean("mission_2", taskCompleted[2])
                .putInt("user_score", userScore)
                .apply();
    }

    public void resetDailyMissionsIfNeeded() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        var prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String lastDate = prefs.getString(PREF_DATE, "");

        if (!today.equals(lastDate)) {
            // Reset nhiệm vụ hằng ngày
            taskCompleted[0] = false;
            taskCompleted[1] = false;
            taskCompleted[2] = false;


            // Ghi ngày mới vào
            prefs.edit()
                    .putString(PREF_DATE, today)
                    .putBoolean("mission_0", false)
                    .putBoolean("mission_1", false)
                    .putBoolean("mission_2", false)
                    .apply();

            determinePetIndex();
        }
    }
    public void init(Context context) {
        this.context = context;

        // Lấy điểm từ backend thông qua AuthViewModel
        if (authViewModel != null) {
            authViewModel.getCurrentUser(); // Gọi API lấy user
            authViewModel.getCurrentUserLiveData().observeForever(user -> {
                if (user != null) {
                    userScore = user.getScore(); // Gán điểm vào userScore
                    determinePetIndex();
                }
            });
        }

        resetDailyMissionsIfNeeded();
        loadMissionState();
    }

    public void loadMissionState() {
        if (context == null) return;

        var prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        taskCompleted[0] = prefs.getBoolean("mission_0", false);
        taskCompleted[1] = prefs.getBoolean("mission_1", false);
        taskCompleted[2] = prefs.getBoolean("mission_2", false);
        userScore = prefs.getInt("user_score", 0);
        determinePetIndex();
    }


}
