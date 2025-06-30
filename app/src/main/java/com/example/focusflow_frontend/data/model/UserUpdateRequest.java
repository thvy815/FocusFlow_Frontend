package com.example.focusflow_frontend.data.model;

public class UserUpdateRequest {
    private String fullName;
    private String username;
    private String avatarUrl;

    public UserUpdateRequest(String fullName, String username, String avatarUrl) {
        this.fullName = fullName;
        this.username = username;
        this.avatarUrl = avatarUrl;
    }

    // Getters
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getAvatarUrl() { return avatarUrl; }

    // Setters
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setUsername(String username) { this.username = username; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
