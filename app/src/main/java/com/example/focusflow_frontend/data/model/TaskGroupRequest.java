package com.example.focusflow_frontend.data.model;

import java.util.List;

public class TaskGroupRequest {
    private Integer taskId;
    public Integer userId;
    public String title;
    public String description;
    public String dueDate;
    public String time;
    public String tag;
    public Integer priority;
    public String repeatStyle;
    public String reminderStyle;
    private List<Integer> ctGroupIds;

    public TaskGroupRequest(Task task, List<Integer> ctGroupIds) {
        this.taskId = task.getId();
        this.userId = task.getUserId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.dueDate = task.getDueDate();
        this.time = task.getTime();
        this.tag = task.getTag();
        this.priority = task.getPriority();
        this.repeatStyle = task.getRepeatStyle();
        this.reminderStyle = task.getReminderStyle();
        this.ctGroupIds = ctGroupIds;
    }

    public Integer getTaskId() { return taskId; }
    public Integer getUserId() { return userId; }
    public List<Integer> getCtGroupIds() { return ctGroupIds; }
}
