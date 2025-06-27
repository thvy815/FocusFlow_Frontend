package com.example.focusflow_frontend.data.model;
public class ProUpgradeRequest {
    private String planName;
    private Long expireTime;

    public ProUpgradeRequest(String planName, Long expireTime) {
        this.planName = planName;
        this.expireTime = expireTime;
    }

    public String getPlanName() {
        return planName;
    }

    public Long getExpireTime() {
        return expireTime;
    }
}
