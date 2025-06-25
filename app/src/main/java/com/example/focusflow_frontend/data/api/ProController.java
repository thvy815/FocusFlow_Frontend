package com.example.focusflow_frontend.data.api;

import com.example.focusflow_frontend.data.model.ProStatusResponse;
import com.example.focusflow_frontend.data.model.ProUpgradeRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ProController {

    // Nâng cấp tài khoản Pro
    @POST("/api/pro/upgrade")
    Call<Void> upgradePro(@Body ProUpgradeRequest request);

    // Lấy trạng thái Pro (lấy từ JWT)
    @GET("/api/pro/status")
    Call<ProStatusResponse> getProStatus();
}
