package com.example.focusflow_frontend.data.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface AvatarService {
    @GET("/avatars/list") // endpoint từ server trả về danh sách URL ảnh
    Call<List<String>> getAvatarUrls();
}
