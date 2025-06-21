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

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PomodoroViewModel extends ViewModel {
    // Hàm lưu pomodoro - userId, taskId, startAt, endAt, dueDate, totalTime, isDeleted
    private final MutableLiveData<Pomodoro> lastCreatedPomodoro = new MutableLiveData<>();
    private TaskController taskController;

    public LiveData<Pomodoro> getLastCreatedPomodoro() {
        return lastCreatedPomodoro;
    }
    public void createPomodoro(Context context, int userId, int taskId,
                             long startAt, LocalDate dueDate) {

        Time st = new Time(startAt);
        String startAtStr = st.toString();
        String dueDateStr = dueDate.toString();

        Pomodoro pomodoro = new Pomodoro(userId, taskId, startAtStr, dueDateStr);

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

    public void createPomodorofull(Context context, int userId, int taskId,
                               long startAt, long endAt, LocalDate dueDate, long totalTime, boolean isDeleted) {

        Time st = new Time(startAt);
        Time en = new Time(endAt);
        String endAtStr = en.toString();
        String startAtStr = st.toString();
        String dueDateStr = dueDate.toString();

        Pomodoro pomodoro = new Pomodoro(userId, taskId, startAtStr, endAtStr, dueDateStr, totalTime, false);

        PomodoroController controller = ApiClient.getPomodoroController(context);

        controller.createPomodoro(pomodoro).enqueue(new Callback<Pomodoro>() {
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

    private final MutableLiveData<Map<String, Integer>> dailyDurationMap = new MutableLiveData<>();

    public LiveData<Map<String, Integer>> getDailyDurationMap() {
        return dailyDurationMap;}

    // Hàm lấy tất cả pomodoro của user
    public void fetchPomodorosByUser(Context context, int userId) {
        PomodoroController controller = ApiClient.getPomodoroController(context);
        controller.getPomodorosByUser(userId).enqueue(new Callback<List<Pomodoro>>() {
            @Override
            public void onResponse(Call<List<Pomodoro>> call, Response<List<Pomodoro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    pomodoroList.postValue(response.body());
                    // Tính tổng thời gian mỗi ngày trong tuần hiện tại
                    calculateWeeklyDurations(response.body());
                } else {
                    Log.e("PomodoroVM", "Failed to get pomodoros: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Pomodoro>> call, Throwable t) {
                Log.e("PomodoroVM", "API error: " + t.getMessage());
            }
        });
    }

    // Lấy mảng 7 ngày tuần hiện tại dạng "yyyy-MM-dd"
    private String[] getCurrentWeekDates() {
        String[] dates = new String[7];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Calendar cal = Calendar.getInstance();
        // Đưa về thứ 2 đầu tuần
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        for (int i = 0; i < 7; i++) {
            dates[i] = sdf.format(cal.getTime());
            cal.add(Calendar.DATE, 1);
        }
        return dates;
    }
    // Tính tổng thời gian (phút) của pomodoro trong tuần hiện tại, theo từng ngày
    private void calculateWeeklyDurations(List<Pomodoro> pomodoros) {
        String[] weekDates = getCurrentWeekDates();
        Map<String, Integer> durationMap = new LinkedHashMap<>();
        for (String date : weekDates) {
            durationMap.put(date, 0);
        }

        for (Pomodoro p : pomodoros) {
            String startAt = p.getStartAt(); // Giả sử format "yyyy-MM-dd HH:mm:ss"
            if (startAt == null) continue;
            String datePart = startAt.split(" ")[0];
            if (durationMap.containsKey(datePart)) {
                int oldDuration = durationMap.get(datePart);
                int addDuration = (int) p.getTotalTime(); // totalTime tính bằng phút
                durationMap.put(datePart, oldDuration + addDuration);
            }
        }
        dailyDurationMap.postValue(durationMap);
    }

    private final MutableLiveData<Pomodoro> latestPomodoro = new MutableLiveData<>();

    public LiveData<Pomodoro> getLatestPomodoro() {
        return latestPomodoro;
    }
    public void fetchLatestPomodoro(Context context, int userId) {
        PomodoroController controller = ApiClient.getPomodoroController(context);

        controller.getPomodorosByUser(userId).enqueue(new Callback<List<Pomodoro>>() {
            @Override
            public void onResponse(Call<List<Pomodoro>> call, Response<List<Pomodoro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Pomodoro> pomodoros = response.body();

                    if (!pomodoros.isEmpty()) {
                        Pomodoro latest = pomodoros.get(0);
                        for (Pomodoro p : pomodoros) {
                            // So sánh startAt dạng String yyyy-MM-dd HH:mm:ss (theo lex order được vì định dạng ISO)
                            if (p.getStartAt().compareTo(latest.getStartAt()) > 0) {
                                latest = p;
                            }
                        }
                        latestPomodoro.postValue(latest);
                    } else {
                        latestPomodoro.postValue(null);
                    }
                } else {
                    Log.e("PomodoroVM", "Failed to get pomodoros: " + response.code());
                    latestPomodoro.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<List<Pomodoro>> call, Throwable t) {
                Log.e("PomodoroVM", "API error: " + t.getMessage());
                latestPomodoro.postValue(null);
            }
        });
    }

    private final MutableLiveData<Map<Integer, String>> taskNameMapLiveData = new MutableLiveData<>();

    public LiveData<Map<Integer, String>> getTaskNameMapLiveData() {
        return taskNameMapLiveData;
    }

    public void fetchTaskNames(Context context, List<Pomodoro> pomodoros) {
        int userId = TokenManager.getUserId(context);

        taskController.getTasksByUser(userId).enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<Integer, String> taskNameMap = new HashMap<>();
                    List<Task> tasks = response.body();
                    for (Pomodoro p : pomodoros) {
                        for (Task t : tasks) {
                            if (t.getId() == p.getTaskId()) {
                                taskNameMap.put(p.getTaskId(), t.getTitle());
                                break;
                            }
                        }
                    }
                    taskNameMapLiveData.setValue(taskNameMap);
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                Log.e("PomodoroViewModel", "Lỗi fetch task name: " + t.getMessage());
            }
        });
    }

    public void deletePomodoroDetail(Context context, int id, boolean check, Runnable onSuccess) {
        PomodoroDetailController controller = ApiClient.getPomodoroDetailController(context);
        Call<Void> call = controller. deletePomodoroDetailsByPomodoroId(id);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (check) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "Deleted detail successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to delete detail. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
                if (response.isSuccessful() && onSuccess != null) {
                    onSuccess.run();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Error deleting detail: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deletePomodoro(Context context, int id, boolean check, Runnable onSuccess) {
        PomodoroController controller = ApiClient.getPomodoroController(context);
        Call<Void> call = controller.deletePomodoro(id);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (check) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "Deleted pomodoro successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to delete pomodoro. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
                if (response.isSuccessful() && onSuccess != null) {
                    onSuccess.run();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Error deleting pomodoro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
