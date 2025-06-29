package com.example.focusflow_frontend.data.model;

public class Pomodoro {

    public Pomodoro(){}

    public Pomodoro(int userId, Integer taskId, String startAt, String endAt, String dueDate, long totalTime, boolean isDeleted) {
        this.userId = userId;
        this.taskId = taskId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.dueDate = dueDate;
        this.totalTime = totalTime;
        this.isDeleted = isDeleted;
    }

    private Integer id;
    private int userId;
    private Integer taskId;
    private String startAt;
    private String endAt;
    private String dueDate;
    private long totalTime;
    private boolean isDeleted;

    public Pomodoro(int userId, Integer taskId, String startAt, String dueDate) {
        this.userId = userId;
        this.taskId = taskId;
        this.startAt = startAt;
        this.dueDate = dueDate;
        this.isDeleted = false;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getTaskId() {
        return taskId;
    }
    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getStartAt() {
        return startAt;
    }
    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public String getEndAt() {
        return endAt;
    }
    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }

    public String getDueDate() {
        return dueDate;
    }
    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public long getTotalTime() {
        return totalTime;
    }
    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public boolean isDeleted() {
        return isDeleted;
    }
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}