package com.example.focusflow_frontend.data.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Streak {
    private Long id;
    private Long user_id;
    private LocalDate start_date;
    private int current_streak;
    private int longest_streak;
    private LocalDateTime last_streak_update;

    // Getters and setters...
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return user_id;
    }

    public void setUserId(Long user_id) {
        this.user_id = user_id;
    }

    public LocalDate getStartDate() {
        return start_date;
    }

    public void setStartDate(LocalDate start_date) {
        this.start_date = start_date;
    }

    public int getCurrentStreak() {
        return current_streak;
    }

    public void setCurrentStreak(int current_streak) {
        this.current_streak = current_streak;
    }

    public int getLongestStreak() {
        return longest_streak;
    }

    public void setLongestStreak(int longest_streak) {
        this.longest_streak = longest_streak;
    }

    public LocalDateTime getLastStreakUpdate() {
        return last_streak_update;
    }

    public void setLastStreakUpdate(LocalDateTime last_streak_update) {
        this.last_streak_update = last_streak_update;
    }
}
