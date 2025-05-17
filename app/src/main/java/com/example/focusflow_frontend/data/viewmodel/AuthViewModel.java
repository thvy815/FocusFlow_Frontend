package com.example.focusflow_frontend.data.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.focusflow_frontend.data.api.UserController;
import com.example.focusflow_frontend.data.model.SignInRequest;
import com.example.focusflow_frontend.data.model.SignInResponse;
import com.example.focusflow_frontend.data.model.User;
import com.example.focusflow_frontend.utils.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends AndroidViewModel {
    private final UserController userController;
    public MutableLiveData<SignInResponse> signInResult = new MutableLiveData<>();
    public MutableLiveData<User> signUpResult = new MutableLiveData<>();
    public MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        userController = ApiClient.getUserController(application.getApplicationContext());
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
}
