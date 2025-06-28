package com.example.focusflow_frontend.data.model;

public class SignInRequest {
    private String emailOrUsername;
    private String password;
    private boolean rememberMe;

    // Constructor, Getters and Setters
    public SignInRequest(String emailOrUsername, String password, boolean rememberMe) {
        this.emailOrUsername = emailOrUsername;
        this.password = password;
        this.rememberMe = rememberMe;
    }

    public String getEmailOrUsername() {
        return emailOrUsername;
    }

    public void setEmailOrUsername(String emailOrUsername) {
        this.emailOrUsername = emailOrUsername;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
