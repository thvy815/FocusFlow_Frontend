package com.example.focusflow_frontend.presentation.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.viewmodel.AuthViewModel;
import com.example.focusflow_frontend.presentation.main.MainActivity;
import com.example.focusflow_frontend.utils.TokenManager;

public class SignInActivity extends AppCompatActivity {
    EditText edtEmail, edtPassword;
    CheckBox cbRememberMe;
    Button btnSignIn;
    AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Nếu token còn thời hạn thì cho phép vào app luôn (không cần đăng nhập)
        if (TokenManager.isRememberMe(this) && TokenManager.getToken(this) != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_sign_in);

        edtEmail = findViewById(R.id.et_email);
        edtPassword = findViewById(R.id.et_password);
        cbRememberMe = findViewById(R.id.cb_remember_me);
        btnSignIn = findViewById(R.id.btn_sign_in);

        // Chuyển hướng Sign Up
        TextView tvToSignUp = findViewById(R.id.tv_sign_up);
        tvToSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // ViewModel
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Sign In
        btnSignIn.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            boolean rememberMe = cbRememberMe.isChecked();
            viewModel.signIn(email, password, rememberMe);
        });

        // Dữ liệu đăng nhập đúng
        viewModel.signInResult.observe(this, result -> {
            TokenManager.saveToken(this, result.getToken());
            TokenManager.saveUserId(this, result.getUserId());

            // Nếu rememberMe, lưu thêm flag
            if (cbRememberMe.isChecked()) {
                TokenManager.saveRememberMe(this, true);
            } else {
                TokenManager.saveRememberMe(this, false);
            }

            Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        // Dữ liệu đăng nhập sai
        viewModel.errorMessage.observe(this, error -> {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }
}
