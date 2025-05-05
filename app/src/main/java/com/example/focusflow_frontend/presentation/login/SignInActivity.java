package com.example.focusflow_frontend.presentation.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.api.UserController;
import com.example.focusflow_frontend.data.model.SignInRequest;
import com.example.focusflow_frontend.data.model.SignInResponse;
import com.example.focusflow_frontend.presentation.main.MainActivity;
import com.example.focusflow_frontend.utils.ApiClient;
import com.example.focusflow_frontend.utils.TokenManager;

import retrofit2.*;

public class SignInActivity extends AppCompatActivity {
    EditText edtEmail, edtPassword;
    Button btnSignIn;
    UserController userController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtEmail = findViewById(R.id.et_email);
        edtPassword = findViewById(R.id.et_password);
        btnSignIn = findViewById(R.id.btn_sign_in);

        // Chuyển hướng Sign Up
        TextView tvToSignUp = findViewById(R.id.tv_sign_up);
        tvToSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Khởi tạo UserController từ ApiClient
        userController = ApiClient.getUserController(SignInActivity.this);

        // Sign In
        btnSignIn.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            // Gửi yêu cầu đăng nhập
            SignInRequest signinRequest = new SignInRequest(email, password);
            userController.signIn(signinRequest).enqueue(new Callback<SignInResponse>() {
                @Override
                public void onResponse(Call<SignInResponse> call, Response<SignInResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Lấy JWT từ response và lưu vào SharedPreferences
                        String token = response.body().getToken();

                        // Lưu token vào SharedPreferences
                        TokenManager.saveToken(SignInActivity.this, token);

                        Toast.makeText(SignInActivity.this, "Sign in successful", Toast.LENGTH_SHORT).show();

                        // Chuyển đến màn hình chính
                        startActivity(new Intent(SignInActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(SignInActivity.this, "Wrong email or password", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SignInResponse> call, Throwable t) {
                    Toast.makeText(SignInActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
