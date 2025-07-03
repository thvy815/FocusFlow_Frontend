package com.example.focusflow_frontend.data.model;

public class ProStatusResponse {
    private boolean isPro;
    private String planName;
    private long expireTime;

    public ProStatusResponse(boolean isPro, String planName, long expireTime) {
        this.isPro = isPro;
        this.planName = planName;
        this.expireTime = expireTime;
    }

    public boolean isPro() {
        return isPro;
    }

    public String getPlanName() {
        return planName;
    }

    public long getExpireTime() {
        return expireTime;
    }

    @Override
    public String toString() {
        return "ProStatusResponse{" +
                "isPro=" + isPro +
                ", planName='" + planName + '\'' +
                ", expireTime=" + expireTime +
                '}';
    }
}
