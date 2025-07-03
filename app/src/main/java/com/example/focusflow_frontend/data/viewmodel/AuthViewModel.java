package com.example.focusflow_frontend.data.viewmodel;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.focusflow_frontend.data.api.UserController;
import com.example.focusflow_frontend.data.model.GoogleLoginRequest;
import com.example.focusflow_frontend.data.model.SignInRequest;
import com.example.focusflow_frontend.data.model.SignInResponse;
import com.example.focusflow_frontend.data.model.SignUpRequest;
import com.example.focusflow_frontend.data.model.User;
import com.example.focusflow_frontend.data.model.UserUpdateRequest;
import com.example.focusflow_frontend.utils.ApiClient;

import java.util.HashMap;
import java.util.List;
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
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final Map<Integer, MutableLiveData<User>> userLiveDataMap = new HashMap<>();
    private final MutableLiveData<List<User>> allUsers = new MutableLiveData<>();
    private final MutableLiveData<Integer> userScore = new MutableLiveData<>(0);
    public LiveData<Integer> getUserScore() {
        return userScore;
    }

    public void increaseScore(int amount) {
        Integer current = userScore.getValue();
        if (current == null) current = 0;
        int updated = current + amount;
        userScore.setValue(updated);
        Log.d("Score", "Score increased immediately to: " + updated);

        // Cập nhật lên server nếu muốn
        updateUserScore(updated);
    }


    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public LiveData<User> getCurrentUserLiveData() {
        return currentUser;
    }

    public AuthViewModel(@NonNull Application application) {
        super(application);
        // Khởi tạo UserController thông qua ApiClient
        Context context = getApplication().getApplicationContext();
        userController = ApiClient.getRetrofit(context).create(UserController.class);
    }

    public void signIn(String usernameOrEmail, String password, boolean rememberMe) {
        SignInRequest request = new SignInRequest(usernameOrEmail, password, rememberMe);
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

    public void signUp(String fullName, String username, String email, String password) {
        SignUpRequest request = new SignUpRequest(fullName, username, email, password);

        userController.signUp(request).enqueue(new Callback<User>() {
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

    public void getCurrentUser() {
        userController.getCurrentUser().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser.postValue(response.body());
                } else {
                    errorMessage.postValue("Không tìm thấy người dùng");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                errorMessage.postValue("Lỗi khi lấy người dùng: " + t.getMessage());
            }
        });
    }

    public void fetchUserById(int userId) {
        userController.getUserById(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (!userLiveDataMap.containsKey(userId)) {
                        userLiveDataMap.put(userId, new MutableLiveData<>());
                    }
                    userLiveDataMap.get(userId).postValue(response.body());
                } else {
                    errorMessage.postValue("Không tìm thấy user với id: " + userId);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                errorMessage.postValue("Lỗi khi lấy user: " + t.getMessage());
            }
        });
    }

    public void fetchAllUsers() {
        userController.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allUsers.postValue(response.body());
                } else {
                    errorMessage.postValue("Không thể tải danh sách người dùng");
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                errorMessage.postValue("Lỗi khi tải người dùng: " + t.getMessage());
            }
        });
    }

    public LiveData<User> getUserByIdLiveData(int userId) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();

        userController.getUserById(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    userLiveData.setValue(response.body());
                } else {
                    userLiveData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                userLiveData.setValue(null);
            }
        });

        return userLiveData;
    }

    public void signInWithGoogle(String idToken) {
        userController.signInWithGoogle(new GoogleLoginRequest(idToken)).enqueue(new Callback<SignInResponse>() {
            @Override
            public void onResponse(Call<SignInResponse> call, Response<SignInResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    signInResult.postValue(response.body());
                } else {
                    errorMessage.postValue("Google sign-in failed: " + response.code());
                    Log.e("GoogleSignIn", "Lỗi response: " + response.code() + " - " + response.message());
                    try {
                        if (response.errorBody() != null) {
                            Log.e("GoogleSignIn", "Lỗi chi tiết: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        Log.e("GoogleSignIn", "Không đọc được lỗi", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<SignInResponse> call, Throwable t) {
                errorMessage.postValue("Google login error: " + t.getMessage());
            }
        });
    }

    public MutableLiveData<Boolean> deleteResult = new MutableLiveData<>();
    public void deleteCurrentUser() {
        userController.deleteCurrentUser().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    deleteResult.postValue(true);
                } else {
                    errorMessage.postValue("Không thể xóa tài khoản: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                errorMessage.postValue("Lỗi mạng khi xóa tài khoản: " + t.getMessage());
            }
        });
    }
    public void updateUser(String fullName, String username, String avatarUrl) {
        UserUpdateRequest request = new UserUpdateRequest(fullName, username, avatarUrl);
        userController.updateUser(request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser.postValue(response.body());
                } else {
                    errorMessage.postValue("Cập nhật thất bại");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                errorMessage.postValue("Lỗi khi cập nhật: " + t.getMessage());
            }
        });
    }
    public void updateUserScore(int score) {
        Map<String, Integer> body = new HashMap<>();
        body.put("score", score);

        userController.updateUserScore(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Log.e("UpdateScore", "Failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("UpdateScore", "Error: " + t.getMessage());
            }
        });
    }

    public MutableLiveData<Boolean> forgotPasswordResult = new MutableLiveData<>();
    public void sendForgotPasswordEmail(String email) {
        userController.forgotPassword(email).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    forgotPasswordResult.postValue(true);
                } else {
                    errorMessage.postValue("Không thể gửi email: " + response.code());
                    forgotPasswordResult.postValue(false);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                errorMessage.postValue("Lỗi khi gửi email: " + t.getMessage());
                forgotPasswordResult.postValue(false);
            }
        });
    }

    public MutableLiveData<Boolean> resetPasswordResult = new MutableLiveData<>();
    public void resetPassword(String token, String newPassword) {
        userController.resetPassword(token, newPassword).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    resetPasswordResult.postValue(true);
                } else {
                    errorMessage.postValue("Token đã hết hạn hoặc không hợp lệ.");
                    resetPasswordResult.postValue(false);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                errorMessage.postValue("Lỗi mạng: " + t.getMessage());
                resetPasswordResult.postValue(false);
            }
        });
    }

    // Hàm gọi lại server để lấy thông tin mới nhất của user
    public void fetchUserInfo(Runnable onComplete) {
        userController.getCurrentUser().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser.postValue(response.body());
                }
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                if (onComplete != null) onComplete.run();
            }
        });
    }

}
