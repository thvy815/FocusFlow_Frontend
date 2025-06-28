package com.example.focusflow_frontend.data.viewmodel;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.focusflow_frontend.data.api.PomodoroController;
import com.example.focusflow_frontend.data.api.PomodoroDetailController;
import com.example.focusflow_frontend.data.api.TaskController;
import com.example.focusflow_frontend.data.model.Pomodoro;
import com.example.focusflow_frontend.data.model.PomodoroDetail;
import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.utils.ApiClient;
import com.example.focusflow_frontend.utils.TokenManager;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PomodoroViewModel extends ViewModel {

    private final MutableLiveData<Pomodoro> lastCreatedPomodoro = new MutableLiveData<>();
    private final MutableLiveData<List<Pomodoro>> pomodoroList = new MutableLiveData<>();
    private final MutableLiveData<List<PomodoroDetail>> pomodoroDetailList = new MutableLiveData<>();
    private final MutableLiveData<Pomodoro> latestPomodoro = new MutableLiveData<>();
    private final MutableLiveData<Map<Integer, String>> taskNameMapLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Integer>> dailyDurationMap = new MutableLiveData<>();
    private TaskController taskController;

    private static final SimpleDateFormat isoDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

    private String formatTime(long timeInMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);
        return isoDateTimeFormat.format(cal.getTime());
    }

    public LiveData<Pomodoro> getLastCreatedPomodoro() {
        return lastCreatedPomodoro;
    }

    public void createPomodoro(Context context, int userId, int taskId, long startAtMillis, LocalDate dueDate) {
        String startAtStr = formatTime(startAtMillis);
        String dueDateStr = dueDate.toString();

        Pomodoro pomodoro = new Pomodoro(userId, taskId, startAtStr, dueDateStr);
        PomodoroController controller = ApiClient.getPomodoroController(context);

        controller.createPomodoro(pomodoro).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Pomodoro> call, Response<Pomodoro> response) {
                if (response.isSuccessful()) {
                    lastCreatedPomodoro.postValue(response.body());
                    Log.d("PomodoroVM", "Pomodoro saved successfully.");
                } else {
                    Log.e("PomodoroVM", "Failed to save 61: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Pomodoro> call, Throwable t) {
                Log.e("PomodoroVM", "Error: " + t.getMessage());
            }
        });
    }

    int checkSucc = 0;
    public int createPomodorofull(Context context, int userId, Integer taskId, long startAt, long endAt, LocalDate dueDate, long totalTime, boolean isDeleted) {
        String startAtStr = formatTime(startAt);
        String endAtStr = formatTime(endAt);
        String dueDateStr = dueDate.toString();

        Pomodoro pomodoro = new Pomodoro(userId, taskId, startAtStr, endAtStr, dueDateStr, totalTime, isDeleted);
        PomodoroController controller = ApiClient.getPomodoroController(context);

        Log.d("PomodoroVM", "Before enqueue");
        controller.createPomodoro(pomodoro).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Pomodoro> call, Response<Pomodoro> response) {
                if (response.isSuccessful()) {
                    Log.d("PomodoroVM", "Pomodoro saved successfully.");
                    checkSucc = 1;
                } else {
                    Log.e("PomodoroVM", "Failed to save 91: " + response.code());
                    checkSucc = 0;
                }
            }
            @Override
            public void onFailure(Call<Pomodoro> call, Throwable t) {
                Log.e("PomodoroVM", "Error: " + t.getMessage());
                checkSucc = 0;
            }
        });
        Log.d("PomodoroVM", "After enqueue");
        return checkSucc;
    }

    public void updatePomodoro(Context context, Pomodoro pomodoro) {
        PomodoroController controller = ApiClient.getPomodoroController(context);

        pomodoro.setStartAt(formatIfNeeded(pomodoro.getStartAt()));
        pomodoro.setEndAt(formatIfNeeded(pomodoro.getEndAt()));

        controller.updatePomodoro(pomodoro.getId(), pomodoro).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Pomodoro> call, Response<Pomodoro> response) {
                if (response.isSuccessful()) {
                    Log.d("PomodoroVM", "Pomodoro updated successfully.");
                } else {
                    Log.e("PomodoroVM", "Failed to update 113: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Pomodoro> call, Throwable t) {
                Log.e("PomodoroVM", "Error: " + t.getMessage());
            }
        });
    }

    private String formatIfNeeded(String rawTime) {
        if (rawTime != null && rawTime.contains("T")) return rawTime;

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(rawTime);
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            return today + "T" + new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date);
        } catch (Exception e) {
            return rawTime;
        }
    }

    public void createPomodoroDetail(Context context, int userId, int taskId, int pomodoroId, long startAt, long endAt, long totalTime) {
        String startAtStr = formatTime(startAt);
        String endAtStr = formatTime(endAt);

        PomodoroDetail detail = new PomodoroDetail(userId, taskId, pomodoroId, startAtStr, endAtStr, totalTime);
        PomodoroDetailController controller = ApiClient.getPomodoroDetailController(context);

        controller.createPomodoroDetail(detail).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<PomodoroDetail> call, Response<PomodoroDetail> response) {
                if (response.isSuccessful()) {
                    Log.d("PomodoroDetailVM", "Detail saved successfully.");
                } else {
                    Log.e("PomodoroDetailVM", "Failed to save detail: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PomodoroDetail> call, Throwable t) {
                Log.e("PomodoroDetailVM", "Error: " + t.getMessage());
            }
        });
    }

    public LiveData<List<Pomodoro>> getPomodoroList() {
        return pomodoroList;
    }

    public void getAllPomodoro(Context context, int userId) {
        PomodoroController controller = ApiClient.getPomodoroController(context);

        controller.getPomodorosByUser(userId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Pomodoro>> call, Response<List<Pomodoro>> response) {
                if (response.isSuccessful()) {
                    pomodoroList.setValue(response.body());
                    Log.d("PomodoroVM", "Fetched all pomodoros.");
                } else {
                    Log.e("PomodoroVM", "Failed to fetch 169: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Pomodoro>> call, Throwable t) {
                Log.e("PomodoroVM", "Error: " + t.getMessage());
            }
        });
    }

    public LiveData<List<PomodoroDetail>> getPomodoroDetailList() {
        return pomodoroDetailList;
    }

    public void getPomodoroDetailsByPomodoroId(Context context, int pomodoroId) {
        PomodoroDetailController controller = ApiClient.getPomodoroDetailController(context);

        controller.getPomodoroDetailsByPomodoroId(pomodoroId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<PomodoroDetail>> call, Response<List<PomodoroDetail>> response) {
                if (response.isSuccessful()) {
                    pomodoroDetailList.setValue(response.body());
                    Log.d("PomodoroVM", "Fetched details.");
                } else {
                    Log.e("PomodoroVM", "Failed to fetch details 197: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<PomodoroDetail>> call, Throwable t) {
                Log.e("PomodoroVM", "Error: " + t.getMessage());
            }
        });
    }

    public LiveData<Map<String, Integer>> getDailyDurationMap() {
        return dailyDurationMap;
    }

    public void fetchPomodorosByUser(Context context, int userId) {
        PomodoroController controller = ApiClient.getPomodoroController(context);

        controller.getPomodorosByUser(userId).enqueue(new Callback<List<Pomodoro>>() {
            @Override
            public void onResponse(Call<List<Pomodoro>> call, Response<List<Pomodoro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Pomodoro> pomodoros = response.body();

                    // Giữ nguyên định dạng ISO
                    pomodoroList.postValue(pomodoros);

                    // Dùng để tính tổng thời gian mỗi ngày trong tuần
                    calculateWeeklyDurations(pomodoros);
                } else {
                    Log.e("PomodoroVM", "Failed to get pomodoros 223: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Pomodoro>> call, Throwable t) {
                Log.e("PomodoroVM", "API error: " + t.getMessage());
            }
        });
    }
    private String[] getCurrentWeekDates() {
        String[] dates = new String[7];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); // Chủ nhật đầu tuần

        for (int i = 0; i < 7; i++) {
            dates[i] = sdf.format(cal.getTime());
            cal.add(Calendar.DATE, 1);
        }
        return dates;
    }
    private void calculateWeeklyDurations(List<Pomodoro> pomodoros) {
        String[] weekDates = getCurrentWeekDates();
        Map<String, Integer> durationMap = new LinkedHashMap<>();

        for (String date : weekDates) {
            durationMap.put(date, 0);
        }
        for (Pomodoro p : pomodoros) {
            String startAt = p.getStartAt();
            if (startAt == null || !startAt.contains("T")) continue;

            String datePart = startAt.split("T")[0];
            if (durationMap.containsKey(datePart)) {
                int current = durationMap.get(datePart); // giữ nguyên (đơn vị: phút)
                int minutes = (int) (p.getTotalTime() / 1000 / 60); // ms → phút
                durationMap.put(datePart, current + minutes);
            }
        }
        dailyDurationMap.postValue(durationMap);
    }

    public LiveData<Pomodoro> getLatestPomodoro() {
        return latestPomodoro;
    }

    public void fetchLatestPomodoro(Context context, int userId) {
        PomodoroController controller = ApiClient.getPomodoroController(context);

        controller.getPomodorosByUser(userId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Pomodoro>> call, Response<List<Pomodoro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Pomodoro> pomodoros = response.body();

                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

                    Pomodoro latest = null;
                    Date latestTime = null;

                    for (Pomodoro p : pomodoros) {
                        try {
                            if (p.getStartAt() == null) continue;
                            Date startDate = isoFormat.parse(p.getStartAt());

                            if (latest == null || (startDate != null && startDate.after(latestTime))) {
                                latest = p;
                                latestTime = startDate;
                            }
                        } catch (Exception e) {
                            Log.w("PomodoroVM", "Parse error: " + e.getMessage());
                        }
                    }

                    latestPomodoro.postValue(latest);
                } else {
                    latestPomodoro.postValue(null);
                    Log.e("PomodoroVM", "Failed to fetch latest 297: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Pomodoro>> call, Throwable t) {
                latestPomodoro.postValue(null);
                Log.e("PomodoroVM", "Error: " + t.getMessage());
            }
        });
    }

    public LiveData<Map<Integer, String>> getTaskNameMapLiveData() {
        return taskNameMapLiveData;
    }

    public void fetchTaskNames(Context context, List<Pomodoro> pomodoros) {
        if (pomodoros == null || pomodoros.isEmpty()) {
            taskNameMapLiveData.setValue(new HashMap<>()); // hoặc giữ nguyên trạng thái cũ
            return;
        }

        int userId = TokenManager.getUserId(context);
        taskController = ApiClient.getTaskController(context);

        taskController.getTasksByUser(userId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<Integer, String> map = new HashMap<>();
                    List<Task> tasks = response.body();
                    for (Pomodoro p : pomodoros) {
                        for (Task t : tasks) {
                            if (t.getId() == p.getTaskId()) {
                                map.put(p.getTaskId(), t.getTitle());
                                break;
                            }
                        }
                    }
                    taskNameMapLiveData.setValue(map);
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                // Optional: log lỗi nếu cần
                Log.e("PomodoroViewModel", "Failed to fetch tasks", t);
            }
        });
    }
    private final MutableLiveData<List<Task>> taskListLiveData = new MutableLiveData<>();

    public LiveData<List<Task>> getTaskListLiveData() {
        return taskListLiveData;
    }

    public void fetchTasks(Context context) {
        int userId = TokenManager.getUserId(context);
        if (taskController == null) {
            taskController = ApiClient.getTaskController(context);
        }
        taskController.getTasksByUser(userId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    taskListLiveData.postValue(response.body());
                } else {
                    Log.e("PomodoroVM", "Failed to fetch tasks: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                Log.e("PomodoroVM", "Error fetching tasks: " + t.getMessage());
            }
        });
    }


    public void deletePomodoroDetail(Context context, int id, boolean check, Runnable onSuccess) {
        PomodoroDetailController controller = ApiClient.getPomodoroDetailController(context);
        controller.deletePomodoroDetailsByPomodoroId(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (check && response.isSuccessful()) {
                    Toast.makeText(context, "Deleted detail successfully", Toast.LENGTH_SHORT).show();
                }
                if (response.isSuccessful() && onSuccess != null) onSuccess.run();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Error deleting detail: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deletePomodoro(Context context, int id, boolean check, Runnable onSuccess) {
        PomodoroController controller = ApiClient.getPomodoroController(context);
        controller.deletePomodoro(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (check && response.isSuccessful()) {
                    Toast.makeText(context, "Deleted pomodoro successfully", Toast.LENGTH_SHORT).show();
                }
                if (response.isSuccessful() && onSuccess != null) onSuccess.run();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Error deleting pomodoro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
