package com.example.focusflow_frontend.data.model;

public class SignInRequest {
    private String email;
    private String password;

    // Constructor, Getters and Setters
    public SignInRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
