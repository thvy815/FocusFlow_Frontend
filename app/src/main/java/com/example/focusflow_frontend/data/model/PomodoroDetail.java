package com.example.focusflow_frontend.data.model;


public class PomodoroDetail {

    private Integer id;
    private Integer userId;

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

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public void setPomodoroId(Integer pomodoroId) {
        this.pomodoroId = pomodoroId;
    }

    private Integer taskId;
    private Integer pomodoroId;
    private String  startAt;
    private String endAt;
    private long totalTime;

    private Boolean isDeleted = false;

    public PomodoroDetail() {}

    public PomodoroDetail(int id, int userId, int taskId, int pomodoroId, String startAt, String endAt, long totalTime) {
        this.id = id;
        this.userId = userId;
        this.taskId = taskId;
        this.pomodoroId = pomodoroId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.totalTime = totalTime;
    }

    public PomodoroDetail(int userId, int taskId, int pomodoroId, String startAt, String endAt, long totalTime) {
        this.userId = userId;
        this.taskId = taskId;
        this.pomodoroId = pomodoroId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.totalTime = totalTime;
    }
    // Getter, setter

    public void setID(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public void setPomodoroId(int pomodoroId) {
        this.pomodoroId = pomodoroId;
    }

    public int getPomodoroId() {
        return pomodoroId;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public String getStartAt() {
        return startAt;
    }

    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }

    public String getEndAt() {
        return endAt;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public boolean getDeleted() {
        return isDeleted;
    }
}
