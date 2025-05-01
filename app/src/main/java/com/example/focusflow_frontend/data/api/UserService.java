package com.example.focusflow_frontend.data.api;

import com.example.focusflow_frontend.data.model.User;

import retrofit2.Call;
import retrofit2.http.*;

public interface UserService {

    @POST("create")
    Call<User> createUser(@Body User user);

    @GET("{email}")
    Call<User> getUser(@Path("email") String email);

    @DELETE("{id}")
    Call<Void> deleteUser(@Path("id") Long id);
}