package com.example.focusflow_frontend.data.model;

public class ProStatusResponse {
    private boolean isPro;
    private String planName;
    private long expireTime;

    public boolean isPro() {
        return isPro;
    }

    public String getPlanName() {
        return planName;
    }

    public long getExpireTime() {
        return expireTime;
    }
}
