package com.example.focusflow_frontend.data.viewmodel;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.focusflow_frontend.data.api.UserController;
import com.example.focusflow_frontend.data.model.SignInRequest;
import com.example.focusflow_frontend.data.model.SignInResponse;
import com.example.focusflow_frontend.data.model.User;
import com.example.focusflow_frontend.utils.ApiClient;
//import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends AndroidViewModel {
    private final UserController userController;
    public MutableLiveData<SignInResponse> signInResult = new MutableLiveData<>();
    public MutableLiveData<User> signUpResult = new MutableLiveData<>();
    public MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> userName = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        // Khởi tạo UserController thông qua ApiClient
        Context context = getApplication().getApplicationContext();
        userController = ApiClient.getRetrofit(context).create(UserController.class);
    }

    public LiveData<String> getUserName() {
        return userName;
    }

    public void signIn(String email, String password, boolean rememberMe) {
        SignInRequest request = new SignInRequest(email, password, rememberMe);
        userController.signIn(request).enqueue(new Callback<SignInResponse>() {
            @Override
            public void onResponse(Call<SignInResponse> call, Response<SignInResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    signInResult.postValue(response.body());
                } else {
                    errorMessage.postValue("Wrong email or password");
                }
            }

            @Override
            public void onFailure(Call<SignInResponse> call, Throwable t) {
                errorMessage.postValue("Error: " + t.getMessage());
            }
        });
    }

    public void signUp(String username, String email, String password) {
        User user = new User(username, email, password);
        userController.createUser(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    signUpResult.postValue(response.body());
                } else {
                    errorMessage.postValue("Sign up failed");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                errorMessage.postValue("Error: " + t.getMessage());
            }
        });
    }

    public void fetchUserName() {
        userController.getCurrentUser().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userName.postValue(response.body().getUsername());
                } else {
                    userName.postValue("Không lấy được tên");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                userName.postValue("Lỗi kết nối");
            }
        });
    }

//    public void updateFcmToken(int userId) {
//        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                String fcmToken = task.getResult();
//
//                Map<String, String> request = new HashMap<>();
//                request.put("userId", String.valueOf(userId));
//                request.put("fcmToken", fcmToken);
//
//                userController.updateFcmToken(request).enqueue(new Callback<ResponseBody>() {
//                    @Override
//                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                        if (response.isSuccessful()) {
//                            Log.d("AuthViewModel", "FCM token updated successfully");
//                        } else {
//                            Log.e("AuthViewModel", "Failed to update FCM token");
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<ResponseBody> call, Throwable t) {
//                        Log.e("AuthViewModel", "Error updating FCM token: " + t.getMessage());
//                    }
//                });
//            } else {
//                Log.e("AuthViewModel", "Failed to get FCM token: " + task.getException());
//            }
//        });
//    }
}
