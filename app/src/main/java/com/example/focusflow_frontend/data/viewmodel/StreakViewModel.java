package com.example.focusflow_frontend.data.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.focusflow_frontend.data.model.Streak;
import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.data.api.StreakRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StreakViewModel extends AndroidViewModel {

    private final MutableLiveData<Integer> streakCountLive = new MutableLiveData<>();
    private final MutableLiveData<List<LocalDate>> datesLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Streak> streakLive = new MutableLiveData<>();

    private final StreakRepository repository;

    public StreakViewModel(@NonNull Application application) {
        super(application);
        this.repository = new StreakRepository(application.getApplicationContext());
    }

    public LiveData<Integer> getStreakCountLive() {
        return streakCountLive;
    }

    public LiveData<List<LocalDate>> getDatesLive() {
        return datesLive;
    }

    public LiveData<Streak> getStreakLive() {
        return streakLive;
    }

    public void fetchStreakData(int userId) {
        repository.fetchStreak(userId, streakLive);
        repository.fetchStreakDates(userId, datesLive);

        // ✅ Gán giá trị cho streakCount khi dữ liệu về
        streakLive.observeForever(streak -> {
            if (streak != null) {
                streakCountLive.postValue(streak.getCurrentStreak());
            }
        });
    }

    // Optional: Local task fallback
    public void checkTasks(List<Task> tasks) {
        List<LocalDate> completedDates = new ArrayList<>();
        for (Task t : tasks) {
            if (Boolean.TRUE.equals(t.isCompleted()) && t.getDueDate() != null) {
                completedDates.add(LocalDate.parse(t.getDueDate()));
            }
        }
        datesLive.setValue(completedDates);
        streakCountLive.setValue(completedDates.size());
    }
}
