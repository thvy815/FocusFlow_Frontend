package com.example.focusflow_frontend.data.model;

import java.io.Serializable;

public class Task implements Serializable {
    private Integer id;
    private int userId;
    private String title;
    private String description;
    private String dueDate;
    private String time;
    private String tag;
    private int priority;
    private String reminderStyle;
    private String repeatStyle;
    private boolean isCompleted = false;

    public Task(){}

    // Constructor
    public Task(int userId, String title, String description, String dueDate, String tag, String time,
                String reminderStyle, int priority, String repeatStyle) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.time = time;
        this.tag = tag;
        this.priority = priority;
        this.reminderStyle = reminderStyle;
        this.repeatStyle = repeatStyle;
        this.isCompleted = false;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getReminderStyle() { return reminderStyle; }
    public void setReminderStyle(String reminderStyle) { this.reminderStyle = reminderStyle; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public String getRepeatStyle() { return repeatStyle; }
    public void setRepeatStyle(String repeatStyle) { this.repeatStyle = repeatStyle; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean isCompleted) { this.isCompleted = isCompleted; }
}
