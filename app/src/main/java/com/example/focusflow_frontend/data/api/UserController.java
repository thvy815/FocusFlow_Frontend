package com.example.focusflow_frontend.data.api;

import com.example.focusflow_frontend.data.model.GoogleLoginRequest;
import com.example.focusflow_frontend.data.model.SignInRequest;
import com.example.focusflow_frontend.data.model.SignInResponse;
import com.example.focusflow_frontend.data.model.SignUpRequest;
import com.example.focusflow_frontend.data.model.User;
import com.example.focusflow_frontend.data.model.UserUpdateRequest;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface UserController {

    // API để tạo người dùng mới
    @POST("auth/signup")
    Call<User> signUp(@Body SignUpRequest request);

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

    @POST("/auth/google-signin")
    Call<SignInResponse> signInWithGoogle(@Body GoogleLoginRequest request);
    @PUT("/api/user/update")
    Call<User> updateUser(@Body UserUpdateRequest updateRequest);

    @PATCH("api/user/score")
    Call<Void> updateUserScore(@Body Map<String, Integer> body);

    @POST("/auth/forgot-password")
    Call<ResponseBody> forgotPassword(@Query("email") String email);

    @POST("/auth/reset-password")
    Call<Void> resetPassword(@Query("token") String token, @Query("newPassword") String newPassword);
    @PUT("api/user/avatar")
    Call<Void> updateAvatar(@Body Map<String, String> avatarBody);
}