package com.example.focusflow_frontend.data.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.focusflow_frontend.data.model.Task; // model task

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StreakViewModel extends ViewModel {

    private final MutableLiveData<List<LocalDate>> datesLive = new MutableLiveData<>();
    private final MutableLiveData<Integer> streakCountLive = new MutableLiveData<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** LiveData danh sách ngày hoàn thành task */
    public LiveData<List<LocalDate>> getDatesLive() {
        return datesLive;
    }
    /** LiveData số ngày liên tục tính từ hôm nay */
    public LiveData<Integer> getStreakCountLive() {
        return streakCountLive;
    }

    /**
     * Tính toán từ danh sách Task (Pomo) để cập nhật ngày và streak count
     */
    public void checkTasks(List<Task> tasks) {
        List<LocalDate> completedDates = new ArrayList<>();
        for (Task t : tasks) {
            if (t.isCompleted()) {
                LocalDate date = LocalDate.parse(t.getDueDate(), formatter);
                completedDates.add(date);
            }
        }
        datesLive.postValue(completedDates);
        computeCurrentStreak(completedDates);
    }

    /**
     * Tính số ngày streak liên tục, bắt đầu từ hôm nay lùi ngược
     */
    private void computeCurrentStreak(List<LocalDate> dates) {
        Set<LocalDate> set = new HashSet<>(dates);
        LocalDate today = LocalDate.now();
        int count = 0;
        while (set.contains(today.minusDays(count))) {
            count++;
        }
        streakCountLive.postValue(count);
    }
}
