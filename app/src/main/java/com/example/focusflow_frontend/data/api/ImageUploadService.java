package com.example.focusflow_frontend.data.api;

import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ImageUploadService {
    @Multipart
    @POST("api/upload/avatar") // Đúng với controller backend
    Call<Map<String, String>> uploadAvatar(@Part MultipartBody.Part file);

}
