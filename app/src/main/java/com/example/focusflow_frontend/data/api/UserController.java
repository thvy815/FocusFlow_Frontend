package com.example.focusflow_frontend.data.api;

import com.example.focusflow_frontend.data.model.SignInRequest;
import com.example.focusflow_frontend.data.model.SignInResponse;
import com.example.focusflow_frontend.data.model.User;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface UserController {

    // API để tạo người dùng mới
    @POST("/api/user/create")
    Call<User> createUser(@Body User user);

    // API để lấy thông tin người dùng hiện tại (email tự lấy từ JWT)
    @GET("/api/user/now")
    Call<User> getCurrentUser();

    // API để lấy user theo id
    @GET("api/user/{id}")
    Call<User> getUserById(@Path("id") int id);

    // API để lấy tất cả người dùng
    @GET("/api/user/all")
    Call<List<User>> getAllUsers();

    // API để xóa người dùng hiện tại (email tự lấy từ JWT)
    @DELETE("/api/user/now")
    Call<Void> deleteCurrentUser();

    // API đăng nhập và nhận JWT
    @POST("/auth/signin")
    Call<SignInResponse> signIn(@Body SignInRequest loginRequest);

    // API update FCM Token
//    @POST("api/user/update-fcm-token")
//    Call<ResponseBody> updateFcmToken(@Body Map<String, String> request);
}