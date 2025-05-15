package com.example.focusflow_frontend.data.model;

public class SignInResponse {
    private String token;
    private int userId;

    // Constructor, Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
