package com.example.focusflow_frontend.data.model;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.util.List;

public class Streak {

    private Integer id;

    @SerializedName("userId")
    private Integer userId;

    @SerializedName("lastValidDate")
    private String lastValidDate;

    @SerializedName("currentStreak")
    private int currentStreak;

    @SerializedName("maxStreak")
    private int maxStreak;

    @SerializedName("validDates")
    private List<String> validDates;

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

    public String getLastValidDate() {
        return lastValidDate;
    }

    public void setLastValidDate(String lastValidDate) {
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

    public List<String> getValidDates() {
        return validDates;
    }

    public void setValidDates(List<String> validDates) {
        this.validDates = validDates;
    }
}
