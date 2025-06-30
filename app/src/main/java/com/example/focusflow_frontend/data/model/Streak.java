package com.example.focusflow_frontend.data.model;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;

public class Streak {

    private Integer id;

    @SerializedName("userId")
    private Integer userId;

    @SerializedName("lastValidDate")
    private LocalDate lastValidDate;

    @SerializedName("currentStreak")
    private int currentStreak;

    @SerializedName("maxStreak")
    private int maxStreak;

    // Getters and setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public LocalDate getLastValidDate() {
        return lastValidDate;
    }

    public void setLastValidDate(LocalDate lastValidDate) {
        this.lastValidDate = lastValidDate;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    public void setMaxStreak(int maxStreak) {
        this.maxStreak = maxStreak;
    }
}
