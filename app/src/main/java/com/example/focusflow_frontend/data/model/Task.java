package com.example.focusflow_frontend.data.model;

import java.time.LocalDate;

public class Task {
    private LocalDate date;
    private boolean completed;

    public Task(LocalDate date, boolean completed) {
        this.date = date;
        this.completed = completed;
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean isCompleted() {
        return completed;
    }
}
