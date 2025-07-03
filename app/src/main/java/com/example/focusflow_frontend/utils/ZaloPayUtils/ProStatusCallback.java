package com.example.focusflow_frontend.utils.ZaloPayUtils;

public interface ProStatusCallback {
    void onResult(boolean isPro);
    void onError(String message);
}